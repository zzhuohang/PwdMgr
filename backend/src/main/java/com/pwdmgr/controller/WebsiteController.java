package com.pwdmgr.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pwdmgr.common.Result;
import com.pwdmgr.dto.WebsiteDTO;
import com.pwdmgr.entity.Website;
import com.pwdmgr.service.WebsiteService;
import com.pwdmgr.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网站控制器
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@RestController
@RequestMapping("/websites")
@RequiredArgsConstructor
public class WebsiteController {

    private final WebsiteService websiteService;
    private final SecurityUtil securityUtil;

    /**
     * 创建网站
     *
     * @param dto 网站信息
     * @return 网站信息
     */
    @PostMapping
    public Result<Website> createWebsite(@Valid @RequestBody WebsiteDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        Website website = websiteService.createWebsite(userId, dto);
        return Result.success("创建成功", website);
    }

    /**
     * 更新网站
     *
     * @param websiteId 网站ID
     * @param dto       网站信息
     * @return 网站信息
     */
    @PutMapping("/{websiteId}")
    public Result<Website> updateWebsite(
            @PathVariable Long websiteId,
            @Valid @RequestBody WebsiteDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        Website website = websiteService.updateWebsite(userId, websiteId, dto);
        return Result.success("更新成功", website);
    }

    /**
     * 删除网站
     *
     * @param websiteId 网站ID
     * @return 操作结果
     */
    @DeleteMapping("/{websiteId}")
    public Result<Void> deleteWebsite(@PathVariable Long websiteId) {
        Long userId = securityUtil.getCurrentUserId();
        websiteService.deleteWebsite(userId, websiteId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取网站详情
     *
     * @param websiteId 网站ID
     * @return 网站信息
     */
    @GetMapping("/{websiteId}")
    public Result<Website> getWebsite(@PathVariable Long websiteId) {
        Long userId = securityUtil.getCurrentUserId();
        Website website = websiteService.getWebsite(userId, websiteId);
        return Result.success(website);
    }

    /**
     * 根据域名获取网站
     *
     * @param domain 域名
     * @return 网站信息
     */
    @GetMapping("/domain/{domain}")
    public Result<Website> getWebsiteByDomain(@PathVariable String domain) {
        Long userId = securityUtil.getCurrentUserId();
        Website website = websiteService.getWebsiteByDomain(userId, domain);
        return Result.success(website);
    }

    /**
     * 分页查询网站列表
     *
     * @param page     页码
     * @param size     每页数量
     * @param keyword  搜索关键词
     * @param category 分类
     * @return 网站列表
     */
    @GetMapping
    public Result<Page<Website>> listWebsites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        Long userId = securityUtil.getCurrentUserId();
        Page<Website> result = websiteService.listWebsites(userId, page, size, keyword, category);
        return Result.success(result);
    }

    /**
     * 获取所有网站分类
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<String>> listCategories() {
        Long userId = securityUtil.getCurrentUserId();
        List<String> categories = websiteService.listCategories(userId);
        return Result.success(categories);
    }
}
