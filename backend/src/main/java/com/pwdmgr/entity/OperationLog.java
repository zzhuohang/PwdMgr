package com.pwdmgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("operation_log")
public class OperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 操作详情
     */
    private String detail;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}