package com.pwdmgr.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果码枚举
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAIL(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "方法不允许"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 数据冲突
     */
    CONFLICT(409, "数据冲突"),

    /**
     * 参数校验失败
     */
    VALIDATION_ERROR(422, "参数校验失败"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统错误"),

    /**
     * 用户名或密码错误
     */
    USERNAME_PASSWORD_ERROR(1001, "用户名或密码错误"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(1002, "用户名已存在"),

    /**
     * 主密码错误
     */
    MASTER_PASSWORD_ERROR(1003, "主密码错误"),

    /**
     * 网站域名已存在
     */
    DOMAIN_EXISTS(1004, "网站域名已存在"),

    /**
     * 凭证不存在
     */
    CREDENTIAL_NOT_FOUND(1005, "凭证不存在"),

    /**
     * 网站不存在
     */
    WEBSITE_NOT_FOUND(1006, "网站不存在"),

    /**
     * 凭证用户名已存在
     */
    CREDENTIAL_USERNAME_EXISTS(1007, "该用户名已存在");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;
}