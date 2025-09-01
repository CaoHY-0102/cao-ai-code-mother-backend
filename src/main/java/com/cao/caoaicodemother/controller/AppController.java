package com.cao.caoaicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cao.caoaicodemother.model.dto.app.*;
import com.cao.caoaicodemother.annotation.AuthCheck;
import com.cao.caoaicodemother.common.BaseResponse;
import com.cao.caoaicodemother.common.DeleteRequest;
import com.cao.caoaicodemother.common.ResultUtils;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.exception.ThrowUtils;
import com.cao.caoaicodemother.model.entity.App;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.vo.AppVO;
import com.cao.caoaicodemother.service.AppService;
import com.cao.caoaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author 小曹同学
 */
@RestController
@Tag(name = "应用信息")
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 【用户】部署应用
     *
     * @param appDeployRequest 应用部署请求
     * @param request          HTTP请求
     * @return 部署URL
     */
    @Operation(summary = "部署应用")
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        //1.参数校验
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        //2.获取登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //3.调用部署服务接口
        String result = appService.deployApp(appId, loginUser);
        // 4.返回部署 URL
        return ResultUtils.success(result);

    }

    /**
     * 【用户】AI生成代码
     *
     * @param appId   应用ID
     * @param message 用户消息
     * @param request HTTP请求
     * @return 生成的代码(流式)
     */
    @Operation(summary = "AI生成代码")
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId, @RequestParam String message, HttpServletRequest request) {
        //1.参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        //3.调用AI生成代码
        User loginUser = userService.getLoginUser(request);
        Flux<String> chatToGenCode = appService.chatToGenCode(appId, message, loginUser);
        return chatToGenCode.map(
                        chunk -> {
                            Map<String, String> result = Map.of("d", chunk);
                            String data = JSONUtil.toJsonStr(result);
                            // 对数据进行加密
                            // String encryptedData = DigestUtil.bcrypt(data);
                            return ServerSentEvent.<String>builder()
                                    .data(data)
                                    .build();
                        })
                .concatWith(
                        // 结束事件
                        Mono.just(
                                ServerSentEvent.<String>builder()
                                        .event("done")
                                        .data("")
                                        .build()));
    }

    /**
     * 【用户】创建应用（须填写 initPrompt）
     *
     * @param appAddRequest 应用创建请求
     * @param request       HTTP请求
     * @return 应用ID
     */
    @Operation(summary = "创建应用")
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        Long appId = appService.createApp(app, loginUser.getId());
        return ResultUtils.success(appId);
    }

    /**
     * 【用户】根据 id 修改自己的应用（目前只支持修改应用名称）
     *
     * @param appUpdateRequest 应用更新请求
     * @param request          HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "修改应用")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(appUpdateRequest, app);
        boolean result = appService.updateApp(app, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 【用户】根据 id 删除自己的应用
     *
     * @param deleteRequest 删除请求
     * @param request       HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "删除应用")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = appService.deleteApp(deleteRequest.getId(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 【用户】根据 id 查看应用详情
     *
     * @param id 应用ID
     * @return 应用详情
     */
    @Operation(summary = "获取应用")
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getAppById(id);
        // 校验权限（只能查看自己的应用）
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无操作权限");
        }
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 【用户】分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @param request         HTTP请求
     * @return 分页结果
     */
    @Operation(summary = "获取应用列表")
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 分页查询用户的应用列表
        Page<App> appPage = appService.listMyAppByPage(appQueryRequest, loginUser.getId());
        // 数据脱敏
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        // 转换为VO对象
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 【用户】分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    @Operation(summary = "获取精选应用列表")
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<App> appPage = appService.listGoodAppByPage(appQueryRequest);
        // 数据脱敏
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 【管理员】根据 id 删除任意应用
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @Operation(summary = "删除应用(管理员)")
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 根据实体主键删除
        boolean result = appService.deleteApp(id, oldApp.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 【管理员】根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     *
     * @param appAdminUpdateRequest 应用更新请求
     * @return 是否成功
     */
    @Operation(summary = "更新应用(管理员)")
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long id = appAdminUpdateRequest.getId();
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 【管理员】分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    @Operation(summary = "获取应用列表(管理员)")
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<App> appPage = appService.listAppByPage(appQueryRequest);
        // 数据脱敏
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 【管理员】根据 id 查看应用详情
     *
     * @param id 应用ID
     * @return 应用详情
     */
    @Operation(summary = "获取应用详情(管理员)")
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getAppById(id);
        return ResultUtils.success(appService.getAppVO(app));
    }
}
