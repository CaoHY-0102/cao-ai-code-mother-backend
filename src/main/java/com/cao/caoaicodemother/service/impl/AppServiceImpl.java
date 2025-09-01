package com.cao.caoaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.constant.AppConstant;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.core.AiCodeGeneratorFacade;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.exception.ThrowUtils;
import com.cao.caoaicodemother.mapper.AppMapper;
import com.cao.caoaicodemother.model.dto.app.AppQueryRequest;
import com.cao.caoaicodemother.model.entity.App;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;
import com.cao.caoaicodemother.model.enums.MessageTypeEnum;
import com.cao.caoaicodemother.model.vo.AppVO;
import com.cao.caoaicodemother.model.vo.UserVO;
import com.cao.caoaicodemother.service.AppService;
import com.cao.caoaicodemother.service.ChatHistoryService;
import com.cao.caoaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 小曹同学
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 2.获取应用
        App app = this.getAppById(appId);
        if (ObjectUtil.isEmpty(app)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 3.仅本人创建的应用可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无操作权限访问应用");
        }
        // 4.生成deployKey(6位大小写字母+数字),deployKey作为文件名
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 获取代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成类型错误");
        }
        // 5.部署操作,将code_output目录下的临时文件移动到code_deployKey目录下
        // 构建源目录(../tmp/code_output/multi_file_appId)
        String sourceName = codeGenTypeEnum.getValue() + "_" + appId;
        String sourcePath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceName;
        File sourceDir = new File(sourcePath);
        if (!FileUtil.exist(sourcePath)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "源目录不存在");
        }
        // 构建部署目录(../tmp/code_deploy/deployKey)
        String deployPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        // 复制文件到部署目录
        try {
            FileUtil.copyContent(sourceDir, new File(deployPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败" + e.getMessage());
        }
        // 6.更新应用信息
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新应用信息失败");
        }
        // 返回可访问的URL(http://localhost/deployKey/)
        return String.format("部署成功,访问地址: %s/%s/", AppConstant.DEPLOY_DOMAIN, deployKey);
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 2.获取应用
        App app = this.getAppById(appId);
        if (ObjectUtil.isEmpty(app)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        Long userId = loginUser.getId();
        // 3.仅本人可以生成代码
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无操作权限访问应用");
        }
        // 4.获取代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成类型错误");
        }
        // 5.将用户消息添加到对话历史
        chatHistoryService.addChatHistory(appId, userId, userMessage, MessageTypeEnum.USER.getValue());
        // 6.调用AI生成代码(流式)
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);
        StringBuffer codeBuilder = new StringBuffer();
        return contentFlux
                .doOnNext(chunk -> {
                    // 实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    String aiResponseMessage = codeBuilder.toString();
                    if (StrUtil.isNotBlank(aiResponseMessage)) {
                        // 将 ai 消息添加到对话历史
                        chatHistoryService.addChatHistory(appId, userId, aiResponseMessage, MessageTypeEnum.AI.getValue());
                    }
                })
                .doOnError(error -> {
                    // 如果 ai 回复错误，也保存错误信息
                    String errorAiResponseMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatHistory(appId, userId, errorAiResponseMessage, MessageTypeEnum.AI.getValue());
                });
    }

    @Override
    public Long createApp(App app, Long userId) {
        // 1. 参数校验
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String initPrompt = app.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化提示词不能为空");
        // 3. 插入数据
        App newApp = new App();
        newApp.setInitPrompt(initPrompt);
        // 应用名称暂时取提示词前 12 位
        newApp.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 默认使用多文件生成
        newApp.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 应用创建者
        newApp.setUserId(userId);
        boolean saveResult = this.save(newApp);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建失败，数据库错误");
        }
        return newApp.getId();
    }

    @Override
    public boolean updateApp(App app, Long userId) {
        if (app == null || app.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 查询原应用
        App oldApp = this.getById(app.getId());
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 校验权限（非管理员只能修改自己的应用）
        if (!oldApp.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无操作权限");
        }
        // 只允许修改名称
        App updateApp = new App();
        updateApp.setId(app.getId());
        if (StrUtil.isNotBlank(app.getAppName())) {
            updateApp.setAppName(app.getAppName());
        }
        updateApp.setEditTime(LocalDateTime.now());
        return this.updateById(updateApp);
    }

    @Override
    public boolean deleteApp(Long id, Long userId) {
        User loginUser = userService.getById(userId);
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        // 查询应用
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 校验权限（非管理员只能删除自己的应用）
        if (!app.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无操作权限");
        }
        // 应用删除时，关联删除应用对应的对话历史记录
        try {
            chatHistoryService.deleteByAppId(id);
        } catch (Exception e) {
            // 记录日志，但不阻止应用删除
            log.error("删除应用时，关联删除应用对应的对话历史记录失败: {}", e.getMessage());
        }
        // 删除应用
        return this.removeById(id);
    }

    @Override
    public App getAppById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        return this.getById(id);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户
        Long userId = appVO.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免出现N+1 查询问题
        List<Long> userIds = appList.stream()
                .map(App::getUserId)
                .distinct()
                .toList();
        // 批量获取用户信息
        Map<Long, UserVO> userIdUserMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> userService.getUserVO(user)));
        return appList.stream().map(app -> {
            AppVO appVO = this.getAppVO(app);
            UserVO userVO = userIdUserMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).toList();
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    public Page<App> listMyAppByPage(AppQueryRequest appQueryRequest, Long userId) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 限制每页最多20个
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        // 只查询当前用户的应用
        queryWrapper.eq("userId", userId);
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    @Override
    public Page<App> listGoodAppByPage(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        // 限制每页最多20个
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        // 只查询 priority = 99 的应用（精选）
        queryWrapper.eq("priority", AppConstant.GOOD_APP_PRIORITY);
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    @Override
    public Page<App> listAppByPage(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        return this.page(Page.of(pageNum, pageSize), queryWrapper);
    }

}
