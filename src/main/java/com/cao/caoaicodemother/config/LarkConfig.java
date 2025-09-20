package com.cao.caoaicodemother.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 飞书配置类
 *
 * @author 小曹同学
 */
@Configuration
@ConfigurationProperties(prefix = "lark")
@Data
public class LarkConfig {
    
    /**
     * 飞书应用的 App ID
     */
    private String clientId;
    
    /**
     * 飞书应用的 App Secret
     */
    private String clientSecret;
    
    /**
     * 飞书授权回调地址
     */
    private String redirectUri;
}