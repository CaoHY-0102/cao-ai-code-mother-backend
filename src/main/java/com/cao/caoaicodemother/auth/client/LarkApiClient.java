package com.cao.caoaicodemother.auth.client;

import com.cao.caoaicodemother.model.entity.LarkUserInfo;
import com.cao.caoaicodemother.model.entity.User;

/**
 * 飞书API客户端接口
 * 定义与飞书API交互的核心方法
 * 
 * @author 小曹同学
 */
public interface LarkApiClient {
    
    /**
     * 通过授权码获取用户访问令牌
     * @param code 授权码
     * @return 访问令牌
     */
    String getUserAccessToken(String code);
    
    /**
     * 通过访问令牌获取用户信息
     * @param accessToken 访问令牌
     * @return 飞书用户信息
     */
    LarkUserInfo getUserInfo(String accessToken);
    
    /**
     * 同步飞书用户信息到数据库
     * @param larkUserInfo 飞书用户信息
     */
    void syncLarkUserInfo(LarkUserInfo larkUserInfo);
    
    /**
     * 创建或更新系统用户
     * @param larkUserInfo 飞书用户信息
     * @return 系统用户
     */
    User createOrUpdateSystemUser(LarkUserInfo larkUserInfo);
}