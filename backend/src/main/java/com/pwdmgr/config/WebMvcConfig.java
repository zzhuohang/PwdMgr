package com.pwdmgr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC 配置
 * - 静态资源映射
 * - SPA 路由回退到 index.html
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置 SPA 路由回退
     * 所有非 /api/** 的路径都转发到 index.html，由 Vue Router 处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 所有非 API 路径，优先匹配静态资源，否则回退到 index.html
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // 如果请求的资源存在（非目录），直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // 否则回退到 index.html（SPA 路由）
                        Resource fallback = new ClassPathResource("/static/index.html");
                        if (fallback.exists()) {
                            return fallback;
                        }
                        return super.getResource(resourcePath, location);
                    }
                });
    }

    /**
     * 首页视图映射
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
