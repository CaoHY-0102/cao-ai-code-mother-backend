package com.cao.caoaicodemother.service;

import com.cao.caoaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.cao.caoaicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.cao.caoaicodemother.model.entity.ChatHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 历史对话 服务层。
 *
 * @author 小曹同学
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 分页获取对话消息列表
     *
     * @param appId           应用Id
     * @param pageSize        分页大小
     * @param loginUser       登录用户
     * @param lastCreateTime  最后创建时间
     * @return
     */
    Page<ChatHistory> listChatHistoryByPage(@Param("appId") Long appId, @Param("pageSize") int pageSize, @Param("loginUser") User loginUser, @Param("lastCreateTime") LocalDateTime lastCreateTime);

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 添加对话消息
     *
     * @param appId       应用Id
     * @param userId      用户Id
     * @param message     消息
     * @param messageType 消息类型
     * @return
     */
    boolean addChatHistory(Long appId, Long userId, String message, String messageType);

    /**
     * 删除聊天历史对话
     * @param appId 应用Id
     */
    boolean deleteByAppId(Long appId);

}
