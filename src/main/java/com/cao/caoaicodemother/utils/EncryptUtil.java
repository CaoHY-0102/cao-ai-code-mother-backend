package com.cao.caoaicodemother.utils;

import cn.hutool.core.codec.Base64;

/**
 * 加密工具类
 */
@Deprecated
public class EncryptUtil {

    // 使用Base64编码作为加密替代方案
    private static final String ENCRYPT_PREFIX = "enc:";

    /**
     * Base64编码（替代加密）
     * @param data 待编码数据
     * @return 编码后的字符串（带前缀）
     */
    public static String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        return ENCRYPT_PREFIX + Base64.encode(data);
    }

    /**
     * Base64解码（替代解密）
     * @param encryptedData 编码后的数据
     * @return 解码后的字符串
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        if (encryptedData.startsWith(ENCRYPT_PREFIX)) {
            return Base64.decodeStr(encryptedData.substring(ENCRYPT_PREFIX.length()));
        }
        return encryptedData;
    }
}