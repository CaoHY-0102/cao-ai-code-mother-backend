package com.cao.caoaicodemother.core.handler;

import cn.hutool.core.util.StrUtil;
import com.cao.caoaicodemother.model.entity.User;
import com.cao.caoaicodemother.model.enums.MessageTypeEnum;
import com.cao.caoaicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        // 实时收集代码片段
        return originFlux
                .doOnNext(aiResponseBuilder::append)
                .doOnComplete(() -> {
                    // 流式返回完成后保存代码
                    String aiResponseMessage = aiResponseBuilder.toString();
                    if (StrUtil.isNotBlank(aiResponseMessage)) {
                        // 将 ai 消息添加到对话历史
                        chatHistoryService.addChatHistory(appId, loginUser.getId(), aiResponseMessage, MessageTypeEnum.AI.getValue());
                    }
                })
                .doOnError(error -> {
                    // 如果 ai 回复错误，也保存错误信息
                    String errorAiResponseMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatHistory(appId, loginUser.getId(), errorAiResponseMessage, MessageTypeEnum.AI.getValue());
                });
    }
}
