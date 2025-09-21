package com.cao.caoaicodemother.auth.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cao.caoaicodemother.config.LarkConfig;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.model.entity.LarkUserInfo;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.service.LarkService;
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
    private LarkService larkService;

    @Resource
    private UserService userService;

    // 飞书API基础URL
    private static final String LARK_API_BASE = "https://open.feishu.cn/open-apis";

    // 获取用户访问令牌的API
    private static final String GET_USER_ACCESS_TOKEN_URL = LARK_API_BASE + "/authen/v2/oauth/token";

    // 获取用户信息的API
    private static final String GET_USER_INFO_URL = LARK_API_BASE + "/authen/v1/user_info";


    @Override
    public String getUserAccessToken(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", larkConfig.getClientId());
        params.put("client_secret", larkConfig.getClientSecret());
        params.put("code", code);
        params.put("redirect_uri", larkConfig.getRedirectUri());

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String response = doPost(GET_USER_ACCESS_TOKEN_URL, headers, params);
        JSONObject jsonResponse = parseResponse(response);

        // 检查响应码
        if (isResponseSuccess(jsonResponse)) {
            String errorMsg = jsonResponse.getStr("msg", "获取访问令牌失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取访问令牌失败: " + errorMsg);
        }

        // 直接从根对象获取access_token，而不是从data字段
        String accessToken = jsonResponse.getStr("access_token");
        if (StrUtil.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取的访问令牌为空");
        }
        return accessToken;
    }

    @Override
    public LarkUserInfo getUserInfo(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json; charset=utf-8");

        String response = doGet(GET_USER_INFO_URL, headers);
        JSONObject jsonResponse = parseResponse(response);

        if (isResponseSuccess(jsonResponse)) {
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

        // 处理头像信息
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

        return larkUserInfo;
    }

    @Override
    public void syncLarkUserInfo(LarkUserInfo larkUserInfo) {
        // 根据unionId查询是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("unionId", larkUserInfo.getUnionId());
        LarkUserInfo existingLarkUser = larkService.getOne(queryWrapper);

        if (existingLarkUser != null) {
            // 如果存在记录，设置ID和原创建时间以确保正确更新
            larkUserInfo.setId(existingLarkUser.getId());
            larkUserInfo.setCreateTime(existingLarkUser.getCreateTime());
        } else {
            // 如果不存在记录，设置创建时间
            larkUserInfo.setCreateTime(LocalDateTime.now());
        }
        
        // 设置更新时间
        larkUserInfo.setUpdateTime(LocalDateTime.now());
        
        // 使用saveOrUpdate方法统一处理保存或更新
        larkService.saveOrUpdate(larkUserInfo);
    }

    @Override
    public User createOrUpdateSystemUser(LarkUserInfo larkUserInfo) {
        // 根据lark_union_id查询是否已存在系统用户
        QueryWrapper userQueryWrapper = new QueryWrapper();
        userQueryWrapper.eq("lark_union_id", larkUserInfo.getUnionId());
        User existingUser = userService.getOne(userQueryWrapper);

        User user = new User();
        if (existingUser != null) {
            // 如果存在用户，设置ID以便更新
            user.setId(existingUser.getId());
            user.setCreateTime(existingUser.getCreateTime()); // 保持原创建时间
        } else {
            // 如果不存在用户，设置默认密码
            String encryptPassword = userService.getEncryptPassword("12345678");
            user.setUserPassword(encryptPassword);
            user.setUserAccount(larkUserInfo.getName());
            user.setUserRole(UserConstant.DEFAULT_ROLE);
            user.setLarkUnionId(larkUserInfo.getUnionId()); // 建立关联
            user.setCreateTime(LocalDateTime.now());
        }
        
        // 通用字段设置
        user.setUserName(StrUtil.isNotBlank(larkUserInfo.getName()) ?
                larkUserInfo.getName() : larkUserInfo.getEnName());
        user.setUserAvatar(larkUserInfo.getAvatarUrl());
        user.setUpdateTime(LocalDateTime.now());
        
        // 使用saveOrUpdate方法统一处理保存或更新
        userService.saveOrUpdate(user);
        
        return userService.getOne(userQueryWrapper);
    }
}