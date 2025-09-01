package com.cao.caoaicodemother.utils.Builder;

/**
 * @author 小曹同学
 * @date 2025/9/1
 */
public class Director {

    private ChatHistoryBuilder chatHistoryBuilder;
    public Director(ChatHistoryBuilder chatHistoryBuilder){
        this.chatHistoryBuilder = chatHistoryBuilder;
    }

    public ChatHistoryTwo construct(){
        chatHistoryBuilder.buildAppId();
        chatHistoryBuilder.buildUserId();
        chatHistoryBuilder.buildMessage();
        chatHistoryBuilder.buildMessageType();
        return chatHistoryBuilder.getResult();
    }

    public static void main(String[] args) {
        ChatHistoryBuilder chatHistoryBuilder = new TryChatHistoryBuilder();
        Director director = new Director(chatHistoryBuilder);
        ChatHistoryTwo chatHistoryTwo = director.construct();
        System.out.println(chatHistoryTwo);
    }
}
