package com.cao.caoaicodemother.model.enums;

import lombok.Getter;

/**
 * 认证类型枚举
 */
@Getter
public enum AuthTypeEnum {

    LARK("飞书", "lark");

    private final String text;

    private final String value;

    AuthTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static AuthTypeEnum getEnumByValue(String value) {
        for (AuthTypeEnum item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }


}
