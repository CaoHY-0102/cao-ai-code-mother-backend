package com.cao.caoaicodemother.auth.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 抽象API客户端
 * 提供第三方API调用的通用功能
 * 
 * @author 小曹同学
 */
public abstract class AbstractApiClient {

    /**
     * 发送GET请求
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应内容
     */
    protected String doGet(String url, Map<String, String> headers) {
        try {
            HttpRequest request = HttpUtil.createGet(url);
            
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::header);
            }
            
            return request.execute().body();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTTP GET请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送POST请求
     * @param url 请求URL
     * @param headers 请求头
     * @param params 请求参数
     * @return 响应内容
     */
    protected String doPost(String url, Map<String, String> headers, Map<String, Object> params) {
        try {
            HttpRequest request = HttpUtil.createPost(url);
            
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::header);
            }
            
            if (params != null && !params.isEmpty()) {
                request.form(params);
            }
            
            return request.execute().body();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTTP POST请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析JSON响应
     * @param response 响应内容
     * @return JSON对象
     */
    protected JSONObject parseResponse(String response) {
        try {
            return JSONUtil.parseObj(response);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析响应失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查响应是否成功
     *
     * @param jsonResponse JSON响应
     * @return 是否成功
     */
    protected boolean isResponseSuccess(JSONObject jsonResponse) {
        if (jsonResponse == null) {
            return false;
        }
        
        int code = jsonResponse.getInt("code", -1);
        return code == 0;
    }
    
    /**
     * 安全地解析JSON对象，处理可能的格式异常
     * @param obj 要解析的对象
     * @return JSON对象，如果解析失败则返回null
     */
    protected JSONObject safeParseJson(Object obj) {
        switch (obj) {
            case null -> {
                return null;
            }
            case JSONObject entries -> {
                return entries;
            }
            case String str -> {
                if (str.trim().startsWith("{")) {
                    try {
                        return JSONUtil.parseObj(str);
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
            default -> {
            }
        }

        return null;
    }
    
    /**
     * URL编码
     * @param value 要编码的值
     * @return 编码后的值
     */
    protected String urlEncode(String value) {
        if (StrUtil.isBlank(value)) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}