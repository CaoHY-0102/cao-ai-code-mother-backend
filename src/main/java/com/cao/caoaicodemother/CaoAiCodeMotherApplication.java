package com.cao.caoaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.cao.caoaicodemother.mapper")
public class CaoAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaoAiCodeMotherApplication.class, args);
    }

}
