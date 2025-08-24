package com.cao.caoaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.constant.AppConstant;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.exception.ThrowUtils;
import com.cao.caoaicodemother.mapper.AppMapper;
import com.cao.caoaicodemother.model.dto.app.AppQueryRequest;
import com.cao.caoaicodemother.model.entity.App;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.enums.CodeGenTypeEnum;
import com.cao.caoaicodemother.model.vo.AppVO;
import com.cao.caoaicodemother.model.vo.UserVO;
import com.cao.caoaicodemother.service.AppService;
import com.cao.caoaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Override
    public Long createApp(App app, Long userId) {
        // 1. 校验
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
        // 创建者
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
