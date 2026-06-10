package com.pwdmgr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 网站DTO
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Data
public class WebsiteDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网站ID(更新时使用)
     */
    private Long id;

    /**
     * 网站域名（可选，软件类账号可不填）
     */
    @Size(max = 255, message = "域名长度不能超过255个字符")
    private String domain;

    /**
     * 网站名称
     */
    @Size(max = 100, message = "网站名称长度不能超过100个字符")
    private String name;

    /**
     * 网站图标URL
     */
    @Size(max = 500, message = "图标URL长度不能超过500个字符")
    private String iconUrl;

    /**
     * 分类
     */
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    /**
     * 登录页面URL
     */
    @Size(max = 500, message = "登录URL长度不能超过500个字符")
    private String loginUrl;

    /**
     * 描述
     */
    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description;
}