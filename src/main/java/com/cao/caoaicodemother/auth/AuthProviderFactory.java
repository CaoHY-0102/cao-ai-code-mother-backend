package com.cao.caoaicodemother.auth;

import com.cao.caoaicodemother.model.enums.AuthTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证提供者工厂
 * 使用工厂模式和策略模式，负责创建和管理不同的认证提供者实例
 * 
 * @author 小曹同学
 */
@Component
public class AuthProviderFactory {

    @Resource
    @Lazy
    private LarkAuthProvider larkAuthProvider;

    // 存储认证提供者实例的缓存
    private final Map<AuthTypeEnum, AbstractAuthProvider> authProviderCache = new ConcurrentHashMap<>();

    /**
     * 获取认证提供者实例
     * 使用单例模式确保每种认证类型只有一个实例
     * 
     * @param authType 认证类型
     * @return 对应的认证提供者实例
     */
    public AbstractAuthProvider getAuthProvider(AuthTypeEnum authType) {
        return authProviderCache.computeIfAbsent(authType, this::getAuthProviderByType);
    }

    /**
     * 根据认证类型获取对应的认证提供者
     * 
     * @param authTypeEnum 认证类型
     * @return 认证提供者实例
     */
    private AbstractAuthProvider getAuthProviderByType(AuthTypeEnum authTypeEnum) {
        switch (authTypeEnum) {
            case LARK -> {
                return larkAuthProvider;
            }
            default -> throw new UnsupportedOperationException("不支持的认证类型: " + authTypeEnum);
        }
    }

    /**
     * 获取飞书认证提供者
     * 提供快捷方法，便于调用
     * 
     * @return 飞书认证提供者实例
     */
    public AbstractAuthProvider getLarkAuthProvider() {
        return getAuthProvider(AuthTypeEnum.LARK);
    }
}