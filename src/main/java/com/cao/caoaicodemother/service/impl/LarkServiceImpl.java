package com.cao.caoaicodemother.service.impl;

import com.cao.caoaicodemother.auth.AbstractAuthProvider;
import com.cao.caoaicodemother.auth.AuthProviderFactory;
import com.cao.caoaicodemother.model.entity.LarkUserInfo;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.vo.LoginUserVO;
import com.cao.caoaicodemother.service.LarkService;
import com.cao.caoaicodemother.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.cao.caoaicodemother.mapper.LarkUserInfoMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 飞书服务实现类
 * 重构为使用抽象认证提供者，提高可扩展性和可维护性
 *
 * @author 小曹同学
 */
@Slf4j
@Service
public class LarkServiceImpl extends ServiceImpl<LarkUserInfoMapper, LarkUserInfo> implements LarkService {

    @Resource
    private AuthProviderFactory authProviderFactory;

    @Resource
    private UserService userService;


    @Override
    public String generateAuthUrl(String state) {
        log.info("生成飞书授权URL，state: {}", state);
        AbstractAuthProvider larkAuthProvider = authProviderFactory.getLarkAuthProvider();
        return larkAuthProvider.generateAuthUrl(state);
    }

    @Override
    public LoginUserVO loginByCode(String code, String state, HttpServletRequest request) {
        log.info("开始飞书登录流程，code: {}, state: {}", code, state);
        
        AbstractAuthProvider larkAuthProvider = authProviderFactory.getLarkAuthProvider();
        // 直接调用loginByCode获取LoginUserVO，它已经包含了会话设置
        LoginUserVO loginUserVO = larkAuthProvider.loginByCode(code, state, request);
        log.info("飞书登录成功，用户: {}", loginUserVO.getUserName());
        
        return loginUserVO;
    }


    @Override
    public User createOrUpdateUser(LarkUserInfo larkUserInfo) {
        log.info("创建或更新用户，unionId: {}", larkUserInfo != null ? larkUserInfo.getUnionId() : null);
        AbstractAuthProvider larkAuthProvider = authProviderFactory.getLarkAuthProvider();
        return larkAuthProvider.createOrUpdateUser(larkUserInfo);
    }

}
