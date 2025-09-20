package com.cao.caoaicodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 飞书用户信息 实体类。
 *
 * @author 小曹同学
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lark_user_info")
public class LarkUserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

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
     * 用户英文名
     */
    @Column("enName")
    private String enName;

    /**
     * 用户头像URL
     */
    @Column("avatarUrl")
    private String avatarUrl;

    /**
     * 用户头像缩略图
     */
    @Column("avatarThumb")
    private String avatarThumb;

    /**
     * 用户头像中等尺寸
     */
    @Column("avatarMiddle")
    private String avatarMiddle;

    /**
     * 用户头像大尺寸
     */
    @Column("avatarBig")
    private String avatarBig;

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
     * 企业邮箱
     */
    @Column("enterpriseEmail")
    private String enterpriseEmail;

    /**
     * 手机号
     */
    @Column("mobile")
    private String mobile;

    /**
     * 租户Key
     */
    @Column("tenantKey")
    private String tenantKey;

    /**
     * 员工工号
     */
    @Column("employeeNo")
    private String employeeNo;
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

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}