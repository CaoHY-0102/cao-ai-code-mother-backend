package com.cao.caoaicodemother.auth;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.auth.client.LarkApiClient;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
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
    public String generateAuthUrl(String state) {
        try {
            return larkApiClient.generateAuthUrl(state);
        } catch (Exception e) {
            log.error("生成飞书授权URL失败，state: {}", state, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成授权URL失败: " + e.getMessage());
        }
    }

    /**
     * 通过授权码获取用户信息并登录（重载版本，不处理会话）
     * @param code 授权码
     * @param state 状态参数
     * @return 用户信息
     */
    public User loginByCode(String code, String state) {
        try {
            log.info("开始飞书登录流程(无会话)，code: {}, state: {}", code, state);

            // 1. 通过授权码获取用户访问令牌
            String accessToken = larkApiClient.getUserAccessToken(code);
            log.info("成功获取飞书访问令牌");

            // 2. 通过访问令牌获取用户信息
            LarkUserInfo larkUserInfo = larkApiClient.getUserInfo(accessToken);
            log.info("成功获取飞书用户信息，unionId: {}", larkUserInfo.getUnionId());

            // 3. 同步飞书用户信息并创建/更新系统用户
            User user = createOrUpdateUser(larkUserInfo);
            log.info("成功同步用户信息，系统用户ID: {}", user.getId());

            return user;

        } catch (Exception e) {
            log.error("飞书登录系统异常，code: {}, state: {}", code, state, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "飞书登录失败: " + e.getMessage());
        }
    }
    
    @Override
    public LoginUserVO loginByCode(String code, String state, HttpServletRequest request) {
        try {
            log.info("开始飞书登录流程，code: {}, state: {}", code, state);

            // 使用重载方法处理核心登录逻辑
            User user = loginByCode(code, state);

            // 设置用户会话
            HttpSession session = request.getSession();
            session.setAttribute(UserConstant.USER_LOGIN_STATE, user);
            log.info("用户会话设置完成");

            // 返回登录用户信息
            LoginUserVO loginUserVO = userService.getLoginUserVO(user);
            log.info("飞书登录成功，用户: {}", loginUserVO.getUserName());

            return loginUserVO;

        } catch (Exception e) {
            log.error("飞书登录系统异常，code: {}, state: {}", code, state, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "飞书登录失败: " + e.getMessage());
        }
    }

    @Override
    public User createOrUpdateUser(Object thirdPartyUserInfo) {
        if (!(thirdPartyUserInfo instanceof LarkUserInfo larkUserInfo)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的飞书用户信息类型");
        }
        
        // 参数校验
        if (StrUtil.isBlank(larkUserInfo.getUnionId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "飞书用户信息无效");
        }

        // 同步飞书用户信息到数据库
        larkApiClient.syncLarkUserInfo(larkUserInfo);

        // 关联/创建系统用户
        User systemUser = createOrUpdateSystemUser(larkUserInfo);

        log.info("飞书用户同步完成，unionId: {}, 系统用户ID: {}",
                larkUserInfo.getUnionId(), systemUser.getId());

        return systemUser;
    }
    
    /**
     * 创建或更新系统用户
     */
    private User createOrUpdateSystemUser(LarkUserInfo larkUserInfo) {
        // 委托给LarkApiClient处理用户创建或更新逻辑
        return larkApiClient.createOrUpdateSystemUser(larkUserInfo);
    }
}