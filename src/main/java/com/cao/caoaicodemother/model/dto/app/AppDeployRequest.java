package com.cao.caoaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 小曹同学
 * @date 2025/8/29
 * @description 应用部署请求
 */
@Data
public class AppDeployRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用id
     */
    private Long appId;
}
