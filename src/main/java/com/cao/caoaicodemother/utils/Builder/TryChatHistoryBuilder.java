package com.cao.caoaicodemother.utils.Builder;

/**
 * @author 小曹同学
 * @date 2025/9/1
 */
public class TryChatHistoryBuilder extends ChatHistoryBuilder {
    @Override
    public void buildAppId() {
        chatHistoryTwo.setAppId(1L);
    }

    @Override
    public void buildUserId() {
        chatHistoryTwo.setUserId(1L);
    }

    @Override
    public void buildMessage() {
        chatHistoryTwo.setMessage("这是测试数据");
    }

    @Override
    public void buildMessageType() {
        chatHistoryTwo.setMessageType("user");
    }
}
