package com.pwdmgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 网站实体类
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("website")
public class Website implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网站ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 网站域名
     */
    private String domain;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站图标URL
     */
    private String iconUrl;

    /**
     * 分类
     */
    private String category;

    /**
     * 登录页面URL
     */
    private String loginUrl;

    /**
     * 描述
     */
    private String description;

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