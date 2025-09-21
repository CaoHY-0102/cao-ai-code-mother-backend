package com.cao.caoaicodemother.auth;

import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 抽象认证提供者
 * 定义认证服务的通用接口
 * 
 * @author 小曹同学
 */
public abstract class AbstractAuthProvider {

    
    /**
     * 通过授权码获取用户信息并登录
     * @param code 授权码
     * @param state 状态参数
     * @param request HTTP请求
     * @return 登录用户信息
     */
    public abstract LoginUserVO loginByCode(String code, String state, HttpServletRequest request);
    
    /**
     * 通过第三方用户信息创建或更新本地用户
     * @param thirdPartyUserInfo 第三方用户信息
     * @return 本地用户信息
     */
    public abstract User createOrUpdateUser(Object thirdPartyUserInfo);
}