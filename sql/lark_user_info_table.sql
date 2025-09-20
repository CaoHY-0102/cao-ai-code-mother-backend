-- 飞书用户信息表
create table if not exists lark_user_info
(
    id                bigint auto_increment comment 'id' primary key,
    userId            varchar(256)                       not null comment '飞书用户ID',
    name              varchar(256)                       null comment '用户姓名',
    enName            varchar(256)                       null comment '用户英文名',
    avatarUrl         varchar(1024)                      null comment '用户头像URL',
    avatarThumb       varchar(1024)                      null comment '用户头像缩略图',
    avatarMiddle      varchar(1024)                      null comment '用户头像中等尺寸',
    avatarBig         varchar(1024)                      null comment '用户头像大尺寸',
    openId            varchar(256)                       null comment '用户在应用内的唯一标识',
    unionId           varchar(256)                       null comment '用户统一ID',
    email             varchar(256)                       null comment '用户邮箱',
    enterpriseEmail   varchar(256)                       null comment '企业邮箱',
    mobile            varchar(64)                        null comment '手机号',
    tenantKey         varchar(256)                       null comment '租户Key',
    employeeNo        varchar(256)                       null comment '员工工号',
    createTime        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete          tinyint  default 0                 not null comment '是否删除'
) comment '飞书用户信息' collate = utf8mb4_unicode_ci;