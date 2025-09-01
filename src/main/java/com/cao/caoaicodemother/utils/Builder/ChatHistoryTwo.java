package com.cao.caoaicodemother.utils.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  实体类。
 *
 * @author 小曹同学
 */

public class ChatHistoryTwo implements Serializable {


    /**
     * id
     */
    private Long id;

    /**
     * 对话内容
     */
    private String message;

    /**
     * user/ai
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "ChatHistoryToBuilder{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", messageType='" + messageType + '\'' +
                ", appId=" + appId +
                ", userId=" + userId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isDelete=" + isDelete +
                '}';
    }
}
