package com.pwdmgr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 凭证DTO
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Data
public class CredentialDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 凭证ID(更新时使用)
     */
    private Long id;

    /**
     * 网站ID
     */
    @NotNull(message = "网站ID不能为空")
    private Long websiteId;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 100, message = "用户名长度不能超过100个字符")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(max = 255, message = "密码长度不能超过255个字符")
    private String password;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String notes;
}