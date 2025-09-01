package com.cao.caoaicodemother.service.impl;

import java.time.LocalDateTime;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.constant.UserConstant;
import com.cao.caoaicodemother.exception.BusinessException;
import com.cao.caoaicodemother.exception.ErrorCode;
import com.cao.caoaicodemother.exception.ThrowUtils;
import com.cao.caoaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.enums.MessageTypeEnum;
import com.mybatisflex.core.paginate.Page;
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
    public Page<ChatHistory> listChatHistoryByPage(Long appId, int pageSize, User loginUser, LocalDateTime lastCreateTime) {
        //1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "分页大小不能超过50");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(lastCreateTime == null, ErrorCode.PARAMS_ERROR, "游标时间不能为空");

        // 2. 校验权限
        // 只有应用创建者和管理员才能查看
        if (!loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE) && !loginUser.getId().equals(appId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看应用的历史对话");
        }
        // 3. 构造查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);
        // 4. 分页查询
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 创建查询条件
        queryWrapper.eq("id", id, id != null && id > 0)
                .eq("message", message, StrUtil.isNotBlank(message))
                .eq("messageType", messageType, StrUtil.isNotBlank(messageType))
                .eq("appId", appId, appId != null && appId > 0)
                .eq("userId", userId, userId != null && userId > 0);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime, true);
        }
        // 排序逻辑
        if (sortField != null) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按照创建时间倒序
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public boolean addChatHistory(Long appId, Long userId, String message, String messageType) {
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
    public boolean deleteByAppId(Long appId) {
        //1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 2. 删除对话内容
        QueryWrapper wrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(wrapper);
    }
}
