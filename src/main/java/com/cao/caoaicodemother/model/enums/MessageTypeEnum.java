package com.cao.caoaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author cao
 * @description: 消息类型枚举
 * @date 2025/9/1 （根据实际生成日期修改）
 */
@Getter
public enum MessageTypeEnum {

    USER("用户消息", "user"),
    AI("AI消息", "ai");

    private final String text;
    private final String value;

    MessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static MessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (MessageTypeEnum anEnum : MessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}