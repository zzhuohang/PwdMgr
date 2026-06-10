package com.pwdmgr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pwdmgr.common.ResultCode;
import com.pwdmgr.dto.WebsiteDTO;
import com.pwdmgr.entity.Website;
import com.pwdmgr.exception.BusinessException;
import com.pwdmgr.repository.WebsiteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 网站服务
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebsiteService {

    private final WebsiteMapper websiteMapper;

    /**
     * 创建网站
     *
     * @param userId 用户ID
     * @param dto    网站信息
     * @return 网站信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Website createWebsite(Long userId, WebsiteDTO dto) {
        // 检查域名是否已存在
        if (websiteMapper.existsByDomain(userId, dto.getDomain())) {
            throw new BusinessException(ResultCode.DOMAIN_EXISTS);
        }

        Website website = new Website();
        website.setUserId(userId);
        website.setDomain(dto.getDomain());
        website.setName(dto.getName());
        website.setIconUrl(dto.getIconUrl());
        website.setCategory(dto.getCategory());
        website.setLoginUrl(dto.getLoginUrl());
        website.setDescription(dto.getDescription());
        website.setCreatedAt(LocalDateTime.now());
        website.setUpdatedAt(LocalDateTime.now());

        websiteMapper.insert(website);

        log.info("创建网站成功: userId={}, domain={}", userId, dto.getDomain());
        return website;
    }

    /**
     * 更新网站
     *
     * @param userId    用户ID
     * @param websiteId 网站ID
     * @param dto       网站信息
     * @return 网站信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Website updateWebsite(Long userId, Long websiteId, WebsiteDTO dto) {
        Website website = websiteMapper.selectById(websiteId);
        if (website == null || !website.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WEBSITE_NOT_FOUND);
        }

        // 检查域名是否已被其他网站使用
        Website existingWebsite = websiteMapper.selectByDomain(userId, dto.getDomain());
        if (existingWebsite != null && !existingWebsite.getId().equals(websiteId)) {
            throw new BusinessException(ResultCode.DOMAIN_EXISTS);
        }

        website.setDomain(dto.getDomain());
        website.setName(dto.getName());
        website.setIconUrl(dto.getIconUrl());
        website.setCategory(dto.getCategory());
        website.setLoginUrl(dto.getLoginUrl());
        website.setDescription(dto.getDescription());
        website.setUpdatedAt(LocalDateTime.now());

        websiteMapper.updateById(website);

        log.info("更新网站成功: userId={}, websiteId={}", userId, websiteId);
        return website;
    }

    /**
     * 删除网站
     *
     * @param userId    用户ID
     * @param websiteId 网站ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWebsite(Long userId, Long websiteId) {
        Website website = websiteMapper.selectById(websiteId);
        if (website == null || !website.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WEBSITE_NOT_FOUND);
        }

        websiteMapper.deleteById(websiteId);

        log.info("删除网站成功: userId={}, websiteId={}", userId, websiteId);
    }

    /**
     * 获取网站详情
     *
     * @param userId    用户ID
     * @param websiteId 网站ID
     * @return 网站信息
     */
    public Website getWebsite(Long userId, Long websiteId) {
        Website website = websiteMapper.selectById(websiteId);
        if (website == null || !website.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WEBSITE_NOT_FOUND);
        }
        return website;
    }

    /**
     * 根据域名获取网站
     *
     * @param userId 用户ID
     * @param domain 域名
     * @return 网站信息
     */
    public Website getWebsiteByDomain(Long userId, String domain) {
        return websiteMapper.selectByDomain(userId, domain);
    }

    /**
     * 分页查询网站列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param size     每页数量
     * @param keyword  搜索关键词
     * @param category 分类
     * @return 网站列表
     */
    public Page<Website> listWebsites(Long userId, Integer page, Integer size, String keyword, String category) {
        Page<Website> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Website> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Website::getUserId, userId);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Website::getName, keyword)
                    .or()
                    .like(Website::getDomain, keyword)
                    .or()
                    .like(Website::getDescription, keyword)
            );
        }

        if (StringUtils.hasText(category)) {
            wrapper.eq(Website::getCategory, category);
        }

        wrapper.orderByDesc(Website::getUpdatedAt);

        return websiteMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 获取用户的所有网站分类
     *
     * @param userId 用户ID
     * @return 分类列表
     */
    public List<String> listCategories(Long userId) {
        return websiteMapper.selectCategories(userId);
    }
}
