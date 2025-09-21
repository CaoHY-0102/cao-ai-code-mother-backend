package com.cao.caoaicodemother.controller;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.exception.ThrowUtils;
import com.cao.caoaicodemother.model.vo.LoginUserVO;
import com.cao.caoaicodemother.service.LarkService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * 飞书登录回调接口 - 优化版
 */
@Controller
@RequestMapping("/lark")
@SuppressWarnings("all")
@Slf4j
public class LarkCallbackController {

    @Resource
    private LarkService larkService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 从配置文件中读取前端地址，避免硬编码
    @Value("${frontend.base-url}")
    private String frontendBaseUrl;


    @Operation(summary = "飞书登录回调")
    @GetMapping("/callback")
    public RedirectView larkCallback(@RequestParam("code") String code,
                                     @RequestParam(value = "state", required = false) String state,
                                     HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR, "授权码不能为空");
        // 通过授权码登录
        LoginUserVO loginUserVO = larkService.loginByCode(code, state, request);
        ThrowUtils.throwIf(loginUserVO == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // 生成令牌或会话信息
        String token = generateSessionToken(loginUserVO);
        // 重定向到前端页面，携带登录成功信息和令牌
        String redirectUrl = buildSuccessRedirectUrl(frontendBaseUrl, token, loginUserVO);
        log.info("飞书登录成功，重定向到前端页面：{}", redirectUrl);
        return new RedirectView(redirectUrl);

    }

    /**
     * 生成会话令牌
     */
    private String generateSessionToken(LoginUserVO userVO) {
        // 1. 生成令牌
        String token = UUID.randomUUID().toString();
        // 2. 将令牌与用户信息存储在缓存中（保持原有逻辑）
        redisTemplate.opsForValue().set("login:token:" + token, userVO, Duration.ofDays(7));
        return token;
    }

    /**
     * 构建成功重定向URL
     */
    private String buildSuccessRedirectUrl(String baseUrl, String token, LoginUserVO userVO) {
        // 使用URLEncoder确保参数安全
        return baseUrl + "?login_success=true" +
                "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8) +
                "&user_id=" + URLEncoder.encode(String.valueOf(userVO.getId()), StandardCharsets.UTF_8) +
                "&user_name=" + URLEncoder.encode(userVO.getUserName(), StandardCharsets.UTF_8);
    }

}