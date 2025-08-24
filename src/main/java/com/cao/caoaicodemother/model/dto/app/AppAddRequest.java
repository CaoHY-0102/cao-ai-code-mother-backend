package com.cao.caoaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用创建请求
 */
@Data
public class AppAddRequest implements Serializable {
    /**
     * 初始化提示词
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}