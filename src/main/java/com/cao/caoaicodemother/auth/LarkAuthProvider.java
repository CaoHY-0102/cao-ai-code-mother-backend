package com.cao.caoaicodemother.auth;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.auth.client.LarkApiClient;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.exception.ThrowUtils;
import com.cao.caoaicodemother.model.entity.LarkUserInfo;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.vo.LoginUserVO;
import com.cao.caoaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 飞书认证提供者
 * 实现飞书平台的认证逻辑
 *
 * @author 小曹同学
 */
@Slf4j
@Component
public class LarkAuthProvider extends AbstractAuthProvider {

    @Resource
    private LarkApiClient larkApiClient;

    @Resource
    private UserService userService;

    @Override
    public LoginUserVO loginByCode(String code, String state, HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR, "授权码不能为空");
        // 2. 通过授权码获取用户访问令牌
        String accessToken = larkApiClient.getUserAccessToken(code);
        // 3. 通过访问令牌获取用户信息
        LarkUserInfo larkUserInfo = larkApiClient.getUserInfo(accessToken);
        // 4. 同步飞书用户信息并创建/更新系统用户
        User user = createOrUpdateUser(larkUserInfo);
        // 5. 设置用户会话
        HttpSession session = request.getSession();
        session.setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 6. 返回登录用户信息
        return userService.getLoginUserVO(user);
    }

    @Override
    public User createOrUpdateUser(Object thirdPartyUserInfo) {
        // 参数类型校验
        if (!(thirdPartyUserInfo instanceof LarkUserInfo larkUserInfo)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的飞书用户信息类型");
        }
        // 参数内容校验
        if (StrUtil.isBlank(larkUserInfo.getUnionId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "unionId 不能为空");
        }
        // 同步飞书用户信息到数据库
        larkApiClient.syncLarkUserInfo(larkUserInfo);
        log.info("成功同步飞书用户信息到数据库，unionId: {}", larkUserInfo.getUnionId());
        // 创建或更新系统用户
        return larkApiClient.createOrUpdateSystemUser(larkUserInfo);
    }
}