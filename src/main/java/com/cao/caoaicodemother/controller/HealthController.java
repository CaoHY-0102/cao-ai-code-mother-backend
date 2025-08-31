package com.cao.caoaicodemother.controller;


import com.cao.caoaicodemother.common.BaseResponse;
import com.cao.caoaicodemother.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
@Tag(name = "健康检查")
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/")
    public BaseResponse<String> health() {
        log.info("health check");
        return ResultUtils.success("ok");
    }
}
