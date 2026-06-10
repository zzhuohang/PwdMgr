package com.pwdmgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账号密码凭证实体类
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credential")
public class Credential implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 凭证ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 网站ID
     */
    private Long websiteId;

    /**
     * 用户名(明文存储)
     */
    private String username;

    /**
     * 密码(加密存储)
     */
    private String password;

    /**
     * 备注
     */
    private String notes;

    /**
     * 密码强度(1-5)
     */
    private Integer strength;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除标志(0:未删除 1:已删除)
     */
    @TableLogic
    private Integer deleted;
}