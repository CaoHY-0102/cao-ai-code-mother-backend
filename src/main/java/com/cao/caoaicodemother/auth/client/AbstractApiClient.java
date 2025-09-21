package com.cao.caoaicodemother.auth.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

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
        HttpRequest request = HttpUtil.createGet(url);

        if (headers != null && !headers.isEmpty()) {
            headers.forEach(request::header);
        }

        return request.execute().body();
    }

    /**
     * 发送POST请求
     * @param url 请求URL
     * @param headers 请求头
     * @param params 请求参数
     * @return 响应内容
     */
    protected String doPost(String url, Map<String, String> headers, Map<String, Object> params) {
        HttpRequest request = HttpUtil.createPost(url);

        if (headers != null && !headers.isEmpty()) {
            headers.entrySet().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                request.header(key, value);
            });
        }

        if (params != null && !params.isEmpty()) {
            request.form(params);
        }

        return request.execute().body();
    }

    /**
     * 解析JSON响应
     * @param response 响应内容
     * @return JSON对象
     */
    protected JSONObject parseResponse(String response) {
        return JSONUtil.parseObj(response);
    }

    /**
     * 检查响应是否成功
     *
     * @param jsonResponse JSON响应
     * @return 是否成功
     */
    protected boolean isResponseSuccess(JSONObject jsonResponse) {
        if (jsonResponse == null) {
            return true;
        }

        int code = jsonResponse.getInt("code", -1);
        return code != 0;
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
}