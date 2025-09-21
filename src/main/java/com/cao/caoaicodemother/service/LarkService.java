package com.cao.caoaicodemother.service;

import com.cao.caoaicodemother.model.entity.LarkUserInfo;
import com.cao.caoaicodemother.model.vo.LoginUserVO;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 飞书服务接口
 *
 * @author 小曹同学
 */
public interface LarkService extends IService<LarkUserInfo> {

    /**
     * 通过授权码获取用户信息并登录
     *
     * @param code 授权码
     * @param state 状态参数
     * @param request HTTP请求
     * @return 登录用户信息
     */
    LoginUserVO loginByCode(String code, String state, HttpServletRequest request);



}
