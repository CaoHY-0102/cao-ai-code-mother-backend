package com.cao.caoaicodemother.auth.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cao.caoaicodemother.config.LarkConfig;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.mapper.LarkUserInfoMapper;
import com.cao.caoaicodemother.model.entity.LarkUserInfo;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 飞书API客户端实现
 * 实现与飞书API交互的具体逻辑
 * 
 * @author 小曹同学
 */
@Slf4j
@Component
public class LarkApiClientImpl extends AbstractApiClient implements LarkApiClient {

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

    // 授权页面URL
    private static final String AUTHORIZATION_URL = "https://open.feishu.cn/open-apis/authen/v1/index";
    
    @Override
    public String generateAuthUrl(String state) {
        String encodedRedirectUri = urlEncode(larkConfig.getRedirectUri());
        String encodedState = urlEncode(state != null ? state : "123");

        return String.format("%s?app_id=%s&redirect_uri=%s&state=%s",
                AUTHORIZATION_URL, larkConfig.getClientId(), encodedRedirectUri, encodedState);
    }

    @Override
    public String getUserAccessToken(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", larkConfig.getClientId());
        params.put("client_secret", larkConfig.getClientSecret());
        params.put("code", code);
        params.put("redirect_uri", larkConfig.getRedirectUri());

        try {
            log.info("请求飞书获取访问令牌，参数：{}", params);

            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            
            String response = doPost(GET_USER_ACCESS_TOKEN_URL, headers, params);

            log.info("飞书获取访问令牌响应：{}", response);
            JSONObject jsonResponse = parseResponse(response);

            // 检查响应码
            if (!isResponseSuccess(jsonResponse, 0)) {
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
        } catch (Exception e) {
            log.error("获取飞书访问令牌失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取访问令牌失败: " + e.getMessage());
        }
    }

    @Override
    public LarkUserInfo getUserInfo(String accessToken) {
        try {
            log.info("请求飞书获取用户信息，accessToken: {}", accessToken);

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);
            headers.put("Content-Type", "application/json; charset=utf-8");
            
            String response = doGet(GET_USER_INFO_URL, headers);

            log.info("飞书获取用户信息响应：{}", response);
            JSONObject jsonResponse = parseResponse(response);

            if (!isResponseSuccess(jsonResponse, 0)) {
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
        } catch (Exception e) {
            log.error("获取飞书用户信息失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public void syncLarkUserInfo(LarkUserInfo larkUserInfo) {
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

    @Override
    public User createOrUpdateSystemUser(LarkUserInfo larkUserInfo) {
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
}