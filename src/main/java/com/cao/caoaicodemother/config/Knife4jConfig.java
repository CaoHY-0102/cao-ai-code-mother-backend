package com.cao.caoaicodemother.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cao
 * @description: Knife4j配置
 * @date 2023/10/23 16:05
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI代码生成平台接口文档")
                        .version("v1.0.0")
                        .description("这是AI代码生成平台的API接口文档，包含所有可用接口的详细说明")
                        .contact(new Contact()
                                .name("小曹同学")
                                .email("caochenxu66@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}