package com.cao.caoaicodemother.service;

import com.mybatisflex.core.service.IService;
import com.cao.caoaicodemother.model.entity.ChatHistory;

/**
 * 历史对话 服务层。
 *
 * @author 小曹同学
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息
     *
     * @param appId       应用Id
     * @param userId      用户Id
     * @param message     消息
     * @param messageType 消息类型
     * @return
     */
    boolean addChatMessage(Long appId, Long userId, String message, String messageType);

    /**
     * 删除聊天历史对话
     * @param appId 应用Id
     */
    boolean deleteChatMessageByAppId(Long appId);

}
