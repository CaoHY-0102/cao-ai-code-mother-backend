package com.cao.caoaicodemother.utils.Builder;

/**
 * @author 小曹同学
 * @date 2025/9/1
 */

/**
 * 抽象建造者
 */
public abstract class ChatHistoryBuilder {

    protected ChatHistoryTwo chatHistoryTwo = new ChatHistoryTwo();

    public abstract void buildAppId();

    public abstract void buildUserId();

    public abstract void buildMessage();

    public abstract void buildMessageType();

    /**
     * 获取结果
     *
     * @return 构建结果
     */
    public ChatHistoryTwo getResult() {
        return chatHistoryTwo;
    }

}
