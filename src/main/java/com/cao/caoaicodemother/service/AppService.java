package com.cao.caoaicodemother.service;

import com.cao.caoaicodemother.model.dto.app.AppQueryRequest;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.cao.caoaicodemother.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用服务层。
 *
 * @author 小曹同学
 */
public interface AppService extends IService<App> {

    /**
     * 删除应用和聊天记录
     *
     * @param id 应用ID
     * @return 是否成功
     */
    boolean deleteChatMessageAndAppById(Long id);
    /**
     * 应用部署
     *
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return 可访问的URL
     */
    String deployApp(Long appId,User loginUser);


    /**
     * 聊天生成代码
     *
     * @param appId 应用ID
     * @param userMessage 用户消息
     * @param loginUser 登录用户
     * @return 流式代码
     */
    Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser);

    /**
     * 创建应用
     *
     * @param app 应用实体
     * @param userId 用户ID
     * @return 应用ID
     */
    Long createApp(App app, Long userId);

    /**
     * 更新应用
     *
     * @param app 应用实体
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateApp(App app, Long userId);

    /**
     * 删除应用
     *
     * @param id 应用ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteApp(Long id, Long userId);

    /**
     * 根据ID获取应用
     *
     * @param id 应用ID
     * @return 应用实体
     */
    App getAppById(Long id);

    /**
     * 获取脱敏的应用信息
     *
     * @param app 应用实体
     * @return 脱敏后的应用信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏的应用列表
     *
     * @param appList 应用列表
     * @return 脱敏后的应用列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 分页查询用户应用列表
     *
     * @param appQueryRequest 查询请求
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<App> listMyAppByPage(AppQueryRequest appQueryRequest, Long userId);

    /**
     * 分页查询精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    Page<App> listGoodAppByPage(AppQueryRequest appQueryRequest);

    /**
     * 分页查询所有应用列表（管理员）
     *
     * @param appQueryRequest 查询请求
     * @return 分页结果
     */
    Page<App> listAppByPage(AppQueryRequest appQueryRequest);
}
