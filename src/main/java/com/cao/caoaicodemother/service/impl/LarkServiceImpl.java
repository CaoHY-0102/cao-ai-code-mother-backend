package com.cao.caoaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cao.caoaicodemother.config.LarkConfig;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.mapper.LarkUserInfoMapper;
import com.cao.caoaicodemother.mapper.UserMapper;
import com.cao.caoaicodemother.model.entity.LarkUserInfo;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.vo.LoginUserVO;
import com.cao.caoaicodemother.service.LarkService;
import com.cao.caoaicodemother.service.UserService;
import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 飞书服务实现类
 *
 * @author 小曹同学
 */
@Slf4j
@Service
public class LarkServiceImpl extends ServiceImpl<LarkUserInfoMapper, LarkUserInfo> implements LarkService {

    @Resource
    private LarkConfig larkConfig;

    @Resource
    private LarkUserInfoMapper larkUserInfoMapper;

    @Resource
    private UserService userService;

    // 飞书API基础URL
    private static final String LARK_API_BASE = "https://open.feishu.cn/open-apis";

    // 获取用户访问令牌的API
    private static final String GET_USER_ACCESS_TOKEN_URL = LARK_API_BASE + "/authen/v2/oauth/token";

    // 获取用户信息的API
    private static final String GET_USER_INFO_URL = LARK_API_BASE + "/authen/v1/user_info";

    // 刷新用户访问令牌的API
    private static final String REFRESH_USER_ACCESS_TOKEN_URL = LARK_API_BASE + "/authen/v2/oauth/token";

    @Override
    public String generateAuthUrl(String state) {
        try {
            String encodedRedirectUri = URLEncoder.encode(larkConfig.getRedirectUri(), StandardCharsets.UTF_8);
            String encodedState = URLEncoder.encode(state != null ? state : "123", StandardCharsets.UTF_8);

            // 使用参考代码中的授权URL格式，与参考代码保持一致
            return String.format("https://open.feishu.cn/open-apis/authen/v1/index?" +
                            "app_id=%s&redirect_uri=%s&state=%s",
                    larkConfig.getClientId(), encodedRedirectUri, encodedState);
        } catch (Exception e) {
            log.error("生成飞书授权URL失败，state: {}", state, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成授权URL失败: " + e.getMessage());
        }
    }

    @Override
    public LoginUserVO loginByCode(String code, String state, HttpServletRequest request) {
        try {
            log.info("开始飞书登录流程，code: {}, state: {}", code, state);

            // 1. 通过授权码获取用户访问令牌
            String accessToken = getUserAccessToken(code);
            log.info("成功获取飞书访问令牌");

            // 2. 通过访问令牌获取用户信息
            LarkUserInfo larkUserInfo = getUserInfo(accessToken);
            log.info("成功获取飞书用户信息，unionId: {}", larkUserInfo.getUnionId());

            // 3. 同步飞书用户信息并创建/更新系统用户
            User user = createOrUpdateUser(larkUserInfo);
            log.info("成功同步用户信息，系统用户ID: {}", user.getId());

            // 4. 设置用户会话
            HttpSession session = request.getSession();
            session.setAttribute(UserConstant.USER_LOGIN_STATE, user);
            log.info("用户会话设置完成");

            // 5. 返回登录用户信息
            LoginUserVO loginUserVO = userService.getLoginUserVO(user);
            log.info("飞书登录成功，用户: {}", loginUserVO.getUserName());

            return loginUserVO;

        } catch (BusinessException e) {
            log.error("飞书登录业务异常，code: {}, state: {}, 错误: {}", code, state, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("飞书登录系统异常，code: {}, state: {}", code, state, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "飞书登录失败: " + e.getMessage());
        }
    }


    @Override
    public User createOrUpdateUser(LarkUserInfo larkUserInfo) {
        // 参数校验
        if (larkUserInfo == null || StrUtil.isBlank(larkUserInfo.getUnionId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "飞书用户信息无效");
        }

        // 第一步：同步飞书用户信息到lark_user_info表
        syncLarkUserInfo(larkUserInfo);

        // 第二步：关联/创建系统用户到user表
        User systemUser = createOrUpdateSystemUser(larkUserInfo);

        log.info("飞书用户同步完成，unionId: {}, 系统用户ID: {}",
                larkUserInfo.getUnionId(), systemUser.getId());

        return systemUser;
    }

    /**
     * 同步飞书用户信息到lark_user_info表
     */
    private void syncLarkUserInfo(LarkUserInfo larkUserInfo) {
        // 根据unionId查询是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("unionId", larkUserInfo.getUnionId());
        LarkUserInfo existingLarkUser = larkUserInfoMapper.selectOneByQuery(queryWrapper);

        if (existingLarkUser != null) {
            // 更新现有记录（保持ID不变）
            larkUserInfo.setId(existingLarkUser.getId());
            larkUserInfo.setCreateTime(existingLarkUser.getCreateTime()); // 保持原创建时间
            larkUserInfo.setUpdateTime(LocalDateTime.now());

            QueryWrapper updateWrapper = new QueryWrapper();
            updateWrapper.eq("id", existingLarkUser.getId());
            larkUserInfoMapper.updateByQuery(larkUserInfo, updateWrapper);

            log.info("更新飞书用户信息，unionId: {}, userId: {}",
                    larkUserInfo.getUnionId(), larkUserInfo.getUserId());
        } else {
            // 插入新记录
            larkUserInfo.setCreateTime(LocalDateTime.now());
            larkUserInfo.setUpdateTime(LocalDateTime.now());
            larkUserInfoMapper.insert(larkUserInfo);

            log.info("创建新飞书用户信息，unionId: {}, userId: {}",
                    larkUserInfo.getUnionId(), larkUserInfo.getUserId());
        }
    }

    /**
     * 创建或更新系统用户
     */
    private User createOrUpdateSystemUser(LarkUserInfo larkUserInfo) {
        // 根据lark_union_id查询是否已存在系统用户
        QueryWrapper userQueryWrapper = new QueryWrapper();
        userQueryWrapper.eq("lark_union_id", larkUserInfo.getUnionId());
        User existingUser = userService.getOne(userQueryWrapper);

        if (existingUser != null) {
            // 更新现有系统用户信息
            existingUser.setUserName(StrUtil.isNotBlank(larkUserInfo.getName()) ?
                    larkUserInfo.getName() : larkUserInfo.getEnName());
            existingUser.setUserAvatar(larkUserInfo.getAvatarUrl());
            existingUser.setUpdateTime(LocalDateTime.now());

            userService.updateById(existingUser);

            log.info("更新系统用户信息，ID: {}, unionId: {}",
                    existingUser.getId(), larkUserInfo.getUnionId());

            return existingUser;
        } else {
            // 加密
            String encryptPassword = userService.getEncryptPassword("12345678");

            // 创建新的系统用户
            User newUser = User.builder()
                    .userAccount(larkUserInfo.getName())
                    .userPassword(encryptPassword)
                    .userName(StrUtil.isNotBlank(larkUserInfo.getName()) ?
                            larkUserInfo.getName() : larkUserInfo.getEnName())
                    .userAvatar(larkUserInfo.getAvatarUrl())
                    .userRole(UserConstant.DEFAULT_ROLE)
                    .larkUnionId(larkUserInfo.getUnionId()) // 建立关联
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            userService.save(newUser);

            log.info("创建新系统用户，ID: {}, unionId: {}",
                    newUser.getId(), larkUserInfo.getUnionId());

            return newUser;
        }
    }

    /**
     * 通过授权码获取用户访问令牌
     */
    private String getUserAccessToken(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", larkConfig.getClientId());
        params.put("client_secret", larkConfig.getClientSecret());
        params.put("code", code);
        params.put("redirect_uri", larkConfig.getRedirectUri());

        try {
            log.info("请求飞书获取访问令牌，参数：{}", params);

            String response = HttpUtil.createPost(GET_USER_ACCESS_TOKEN_URL)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .form(params)
                    .execute()
                    .body();

            log.info("飞书获取访问令牌响应：{}", response);
            JSONObject jsonResponse = JSONUtil.parseObj(response);

            // 检查响应码
            if (jsonResponse.getInt("code", -1) != 0) {
                String errorMsg = jsonResponse.getStr("msg", "获取访问令牌失败");
                log.error("飞书获取访问令牌失败，错误码：{}，错误信息：{}",
                        jsonResponse.getInt("code"), errorMsg);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取访问令牌失败: " + errorMsg);
            }

            // 直接从根对象获取access_token，而不是从data字段
            String accessToken = jsonResponse.getStr("access_token");
            if (StrUtil.isBlank(accessToken)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取的访问令牌为空");
            }

            return accessToken;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取飞书访问令牌失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取访问令牌失败: " + e.getMessage());
        }
    }

    /**
     * 通过访问令牌获取用户信息
     */
    private LarkUserInfo getUserInfo(String accessToken) {
        try {
            log.info("请求飞书获取用户信息，accessToken: {}", accessToken);

            String response = HttpUtil.createGet(GET_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .execute()
                    .body();

            log.info("飞书获取用户信息响应：{}", response);

            JSONObject jsonResponse = JSONUtil.parseObj(response);

            if (jsonResponse.getInt("code") != 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取用户信息失败: " + jsonResponse.getStr("msg"));
            }

            JSONObject data = jsonResponse.getJSONObject("data");

            // 构建飞书用户信息对象
            LarkUserInfo larkUserInfo = LarkUserInfo.builder()
                    .userId(data.getStr("user_id"))
                    .name(data.getStr("name"))
                    .enName(data.getStr("en_name"))
                    .avatarUrl(data.getStr("avatar_url"))
                    .openId(data.getStr("open_id"))
                    .unionId(data.getStr("union_id"))
                    .email(data.getStr("email"))
                    .enterpriseEmail(data.getStr("enterprise_email"))
                    .mobile(data.getStr("mobile"))
                    .tenantKey(data.getStr("tenant_key"))
                    .employeeNo(data.getStr("employee_no"))
                    .build();

            // 安全处理头像信息
            try {
                // 处理头像缩略图
                if (data.containsKey("avatar_thumb")) {
                    Object avatarThumbObj = data.get("avatar_thumb");
                    JSONObject avatarThumbJson = safeParseJson(avatarThumbObj);
                    if (avatarThumbJson != null) {
                        larkUserInfo.setAvatarThumb(avatarThumbJson.getStr("avatar_72"));
                    } else if (avatarThumbObj instanceof String) {
                        larkUserInfo.setAvatarThumb((String) avatarThumbObj);
                    }
                }

                // 处理头像中等尺寸
                if (data.containsKey("avatar_middle")) {
                    Object avatarMiddleObj = data.get("avatar_middle");
                    JSONObject avatarMiddleJson = safeParseJson(avatarMiddleObj);
                    if (avatarMiddleJson != null) {
                        larkUserInfo.setAvatarMiddle(avatarMiddleJson.getStr("avatar_240"));
                    } else if (avatarMiddleObj instanceof String) {
                        larkUserInfo.setAvatarMiddle((String) avatarMiddleObj);
                    }
                }

                // 处理头像大尺寸
                if (data.containsKey("avatar_big")) {
                    Object avatarBigObj = data.get("avatar_big");
                    JSONObject avatarBigJson = safeParseJson(avatarBigObj);
                    if (avatarBigJson != null) {
                        larkUserInfo.setAvatarBig(avatarBigJson.getStr("avatar_640"));
                    } else if (avatarBigObj instanceof String) {
                        larkUserInfo.setAvatarBig((String) avatarBigObj);
                    }
                }
            } catch (Exception e) {
                log.warn("处理头像信息时出现异常，使用默认值", e);
                // 设置默认头像URL
                if (larkUserInfo.getAvatarUrl() != null) {
                    larkUserInfo.setAvatarThumb(larkUserInfo.getAvatarUrl());
                    larkUserInfo.setAvatarMiddle(larkUserInfo.getAvatarUrl());
                    larkUserInfo.setAvatarBig(larkUserInfo.getAvatarUrl());
                }
            }

            log.info("成功获取飞书用户信息，unionId: {}, name: {}",
                    larkUserInfo.getUnionId(), larkUserInfo.getName());

            return larkUserInfo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取飞书用户信息失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 刷新用户访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String refreshUserAccessToken(String refreshToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        params.put("client_id", larkConfig.getClientId());
        params.put("client_secret", larkConfig.getClientSecret());

        try {
            log.info("请求飞书刷新访问令牌，refreshToken: {}", refreshToken);

            String response = HttpUtil.createPost(REFRESH_USER_ACCESS_TOKEN_URL)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .form(params)
                    .execute()
                    .body();

            log.info("飞书刷新访问令牌响应：{}", response);
            JSONObject jsonResponse = JSONUtil.parseObj(response);

            // 检查响应码
            if (jsonResponse.getInt("code", -1) != 0) {
                String errorMsg = jsonResponse.getStr("msg", "刷新访问令牌失败");
                log.error("飞书刷新访问令牌失败，错误码：{}，错误信息：{}",
                        jsonResponse.getInt("code"), errorMsg);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "刷新访问令牌失败: " + errorMsg);
            }

            // 解析新的访问令牌
            JSONObject data = jsonResponse.getJSONObject("data");
            if (data == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "刷新令牌响应数据为空");
            }

            String accessToken = data.getStr("access_token");
            if (StrUtil.isBlank(accessToken)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "刷新后的访问令牌为空");
            }

            log.info("成功刷新飞书访问令牌");
            return accessToken;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("刷新飞书访问令牌失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "刷新访问令牌失败: " + e.getMessage());
        }
    }

    /**
     * 安全地解析JSON对象，处理可能的格式异常
     */
    private JSONObject safeParseJson(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }

        if (obj instanceof String str) {
            if (str.trim().startsWith("{")) {
                try {
                    return JSONUtil.parseObj(str);
                } catch (Exception e) {
                    log.warn("解析JSON字符串失败: {}", str, e);
                    return null;
                }
            }
        }

        return null;
    }
}
