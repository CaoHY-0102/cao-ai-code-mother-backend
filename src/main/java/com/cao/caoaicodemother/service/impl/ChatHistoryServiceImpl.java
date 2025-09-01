package com.cao.caoaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.exception.ThrowUtils;
import com.cao.caoaicodemother.model.enums.MessageTypeEnum;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.cao.caoaicodemother.model.entity.ChatHistory;
import com.cao.caoaicodemother.mapper.ChatHistoryMapper;
import com.cao.caoaicodemother.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 历史对话 服务层实现。
 *
 * @author 小曹同学
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Override
    public boolean addChatMessage(Long appId, Long userId, String message, String messageType) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "消息类型错误");
        // 2. 保存对话内容
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteChatMessageByAppId(Long appId) {
        //1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        // 2. 删除对话内容
        QueryWrapper wrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(wrapper);
    }
}
