package com.cao.caoaicodemother.model.vo;

import com.mybatisflex.annotation.Column;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author cao
 * @description: 飞书用户信息VO
 * @date 2023/8/16 10:09
 */
@Data
@Builder
public class LarkUserInfoVO implements Serializable {


    /**
     * 飞书用户ID
     */
    @Column("userId")
    private String userId;

    /**
     * 用户姓名
     */
    @Column("name")
    private String name;

    /**
     * 用户在应用内的唯一标识
     */
    @Column("openId")
    private String openId;

    /**
     * 用户统一ID
     */
    @Column("unionId")
    private String unionId;

    /**
     * 用户邮箱
     */
    @Column("email")
    private String email;


    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;


    private static final long serialVersionUID = 1L;
}
