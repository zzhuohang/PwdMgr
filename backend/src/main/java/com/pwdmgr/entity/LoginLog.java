package com.pwdmgr.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("login_log")
public class LoginLog implements Serializable {

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
     * 登录时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 状态(1:成功 0:失败)
     */
    private Integer status;

    /**
     * 消息
     */
    private String message;
}