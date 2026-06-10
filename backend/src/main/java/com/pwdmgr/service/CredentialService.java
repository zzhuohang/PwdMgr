package com.pwdmgr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pwdmgr.common.ResultCode;
import com.pwdmgr.dto.CredentialDTO;
import com.pwdmgr.entity.Credential;
import com.pwdmgr.entity.Website;
import com.pwdmgr.exception.BusinessException;
import com.pwdmgr.repository.CredentialMapper;
import com.pwdmgr.repository.WebsiteMapper;
import com.pwdmgr.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 凭证服务
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialService {

    private final CredentialMapper credentialMapper;
    private final WebsiteMapper websiteMapper;
    private final EncryptionUtil encryptionUtil;

    /**
     * 创建凭证
     *
     * @param userId     用户ID
     * @param masterKey  主密钥
     * @param dto        凭证信息
     * @return 凭证信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Credential createCredential(Long userId, String masterKey, CredentialDTO dto) {
        // 验证网站是否存在
        Website website = websiteMapper.selectById(dto.getWebsiteId());
        if (website == null || !website.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WEBSITE_NOT_FOUND);
        }

        // 检查同一网站下用户名是否重复
        LambdaQueryWrapper<Credential> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Credential::getUserId, userId)
                .eq(Credential::getWebsiteId, dto.getWebsiteId())
                .eq(Credential::getUsername, dto.getUsername());
        if (credentialMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException(ResultCode.CREDENTIAL_USERNAME_EXISTS);
        }

        Credential credential = new Credential();
        credential.setUserId(userId);
        credential.setWebsiteId(dto.getWebsiteId());
        // 用户名明文存储，密码加密存储
        credential.setUsername(dto.getUsername());
        credential.setPassword(encryptionUtil.encrypt(dto.getPassword(), masterKey));
        credential.setNotes(dto.getNotes());
        credential.setStrength(encryptionUtil.calculatePasswordStrength(dto.getPassword()));
        credential.setCreatedAt(LocalDateTime.now());
        credential.setUpdatedAt(LocalDateTime.now());

        credentialMapper.insert(credential);

        log.info("创建凭证成功: userId={}, websiteId={}", userId, dto.getWebsiteId());
        return credential;
    }

    /**
     * 更新凭证
     *
     * @param userId      用户ID
     * @param masterKey   主密钥
     * @param credentialId 凭证ID
     * @param dto         凭证信息
     * @return 凭证信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Credential updateCredential(Long userId, String masterKey, Long credentialId, CredentialDTO dto) {
        Credential credential = credentialMapper.selectById(credentialId);
        if (credential == null || !credential.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CREDENTIAL_NOT_FOUND);
        }

        // 验证网站是否存在
        Website website = websiteMapper.selectById(dto.getWebsiteId());
        if (website == null || !website.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WEBSITE_NOT_FOUND);
        }

        // 如果用户名变了，检查新用户名是否重复
        if (!credential.getUsername().equals(dto.getUsername())) {
            LambdaQueryWrapper<Credential> dupWrapper = new LambdaQueryWrapper<>();
            dupWrapper.eq(Credential::getUserId, userId)
                    .eq(Credential::getWebsiteId, dto.getWebsiteId())
                    .eq(Credential::getUsername, dto.getUsername())
                    .ne(Credential::getId, credentialId);
            if (credentialMapper.selectCount(dupWrapper) > 0) {
                throw new BusinessException(ResultCode.CREDENTIAL_USERNAME_EXISTS);
            }
        }

        credential.setWebsiteId(dto.getWebsiteId());
        // 用户名明文存储，密码加密存储
        credential.setUsername(dto.getUsername());
        credential.setPassword(encryptionUtil.encrypt(dto.getPassword(), masterKey));
        credential.setNotes(dto.getNotes());
        credential.setStrength(encryptionUtil.calculatePasswordStrength(dto.getPassword()));
        credential.setUpdatedAt(LocalDateTime.now());

        credentialMapper.updateById(credential);

        log.info("更新凭证成功: userId={}, credentialId={}", userId, credentialId);
        return credential;
    }

    /**
     * 删除凭证
     *
     * @param userId       用户ID
     * @param credentialId 凭证ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCredential(Long userId, Long credentialId) {
        Credential credential = credentialMapper.selectById(credentialId);
        if (credential == null || !credential.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CREDENTIAL_NOT_FOUND);
        }

        credentialMapper.deleteById(credentialId);

        log.info("删除凭证成功: userId={}, credentialId={}", userId, credentialId);
    }

    /**
     * 获取凭证详情（密码解密，用户名明文）
     *
     * @param userId       用户ID
     * @param masterKey    主密钥
     * @param credentialId 凭证ID
     * @return 凭证信息
     */
    public Map<String, Object> getCredential(Long userId, String masterKey, Long credentialId) {
        Credential credential = credentialMapper.selectById(credentialId);
        if (credential == null || !credential.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CREDENTIAL_NOT_FOUND);
        }

        // 更新最后使用时间
        credential.setLastUsedTime(LocalDateTime.now());
        credentialMapper.updateById(credential);

        // 解密数据
        Website website = websiteMapper.selectById(credential.getWebsiteId());

        return Map.of(
                "id", credential.getId(),
                "websiteId", credential.getWebsiteId(),
                "websiteName", website != null ? website.getName() : "",
                "websiteDomain", website != null ? website.getDomain() : "",
                "username", credential.getUsername(),
                "password", encryptionUtil.decrypt(credential.getPassword(), masterKey),
                "notes", credential.getNotes() != null ? credential.getNotes() : "",
                "strength", credential.getStrength(),
                "lastUsedTime", credential.getLastUsedTime() != null ? credential.getLastUsedTime() : "",
                "createdAt", credential.getCreatedAt()
        );
    }

    /**
     * 根据域名获取凭证列表（解密）
     *
     * @param userId    用户ID
     * @param masterKey 主密钥
     * @param domain    域名
     * @return 凭证列表（已解密）
     */
    public List<Map<String, Object>> getCredentialsByDomain(Long userId, String masterKey, String domain) {
        List<Credential> credentials = credentialMapper.selectByDomain(userId, domain);

        return credentials.stream().map(credential -> {
            Website website = websiteMapper.selectById(credential.getWebsiteId());
            return Map.<String, Object>of(
                    "id", credential.getId(),
                    "websiteId", credential.getWebsiteId(),
                    "websiteName", website != null ? website.getName() : "",
                    "websiteDomain", website != null ? website.getDomain() : "",
                    "username", credential.getUsername(),
                    "password", encryptionUtil.decrypt(credential.getPassword(), masterKey),
                    "notes", credential.getNotes() != null ? credential.getNotes() : "",
                    "strength", credential.getStrength()
            );
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询凭证列表（不解密，仅显示基本信息）
     *
     * @param userId    用户ID
     * @param page      页码
     * @param size      每页数量
     * @param websiteId 网站ID
     * @param keyword   搜索关键词
     * @return 凭证列表（含 websiteName）
     */
    public Page<Map<String, Object>> listCredentials(Long userId, Integer page, Integer size, Long websiteId, String keyword) {
        Page<Credential> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Credential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Credential::getUserId, userId);

        if (websiteId != null) {
            wrapper.eq(Credential::getWebsiteId, websiteId);
        }

        // 处理关键词搜索（搜索用户名或网站名）
        if (StringUtils.hasText(keyword)) {
            // 先查找匹配关键词的网站ID
            LambdaQueryWrapper<Website> websiteWrapper = new LambdaQueryWrapper<>();
            websiteWrapper.eq(Website::getUserId, userId)
                    .and(w -> w
                            .like(Website::getName, keyword)
                            .or()
                            .like(Website::getDomain, keyword)
                    );
            List<Website> matchedWebsites = websiteMapper.selectList(websiteWrapper);
            List<Long> matchedWebsiteIds = matchedWebsites.stream()
                    .map(Website::getId)
                    .collect(Collectors.toList());

            // 搜索用户名或匹配的网站ID
            wrapper.and(w -> w
                    .like(Credential::getUsername, keyword)
                    .or()
                    .in(Credential::getWebsiteId, matchedWebsiteIds)
            );
        }

        wrapper.orderByDesc(Credential::getUpdatedAt);

        Page<Credential> credentialPage = credentialMapper.selectPage(pageParam, wrapper);

        // 收集所有 websiteId，批量查询网站名
        List<Long> websiteIds = credentialPage.getRecords().stream()
                .map(Credential::getWebsiteId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> websiteNameMap = new HashMap<>();
        Map<Long, String> websiteDomainMap = new HashMap<>();
        if (!websiteIds.isEmpty()) {
            LambdaQueryWrapper<Website> webWrapper = new LambdaQueryWrapper<>();
            webWrapper.in(Website::getId, websiteIds);
            for (Website w : websiteMapper.selectList(webWrapper)) {
                websiteNameMap.put(w.getId(), w.getName());
                websiteDomainMap.put(w.getId(), w.getDomain());
            }
        }

        // 组装带 websiteName 的结果
        Page<Map<String, Object>> result = new Page<>();
        result.setCurrent(credentialPage.getCurrent());
        result.setSize(credentialPage.getSize());
        result.setTotal(credentialPage.getTotal());
        result.setRecords(credentialPage.getRecords().stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("userId", c.getUserId());
            map.put("websiteId", c.getWebsiteId());
            map.put("websiteName", websiteNameMap.getOrDefault(c.getWebsiteId(), ""));
            map.put("websiteDomain", websiteDomainMap.getOrDefault(c.getWebsiteId(), ""));
            map.put("username", c.getUsername());
            map.put("password", c.getPassword());
            map.put("notes", c.getNotes());
            map.put("strength", c.getStrength());
            map.put("lastUsedTime", c.getLastUsedTime());
            map.put("createdAt", c.getCreatedAt());
            map.put("updatedAt", c.getUpdatedAt());
            return map;
        }).collect(Collectors.toList()));

        return result;
    }

    /**
     * 获取用户凭证统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getCredentialStats(Long userId) {
        // 总凭证数
        LambdaQueryWrapper<Credential> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Credential::getUserId, userId);
        Long credentialCount = credentialMapper.selectCount(totalWrapper);

        // 网站数量
        LambdaQueryWrapper<Website> websiteWrapper = new LambdaQueryWrapper<>();
        websiteWrapper.eq(Website::getUserId, userId);
        Long websiteCount = websiteMapper.selectCount(websiteWrapper);

        // 弱密码数量（强度<=2）
        LambdaQueryWrapper<Credential> weakWrapper = new LambdaQueryWrapper<>();
        weakWrapper.eq(Credential::getUserId, userId)
                .le(Credential::getStrength, 2);
        Long weakPasswordCount = credentialMapper.selectCount(weakWrapper);

        // 过期密码数量（假设30天未使用为过期）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<Credential> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.eq(Credential::getUserId, userId)
                .lt(Credential::getLastUsedTime, thirtyDaysAgo)
                .or()
                .eq(Credential::getUserId, userId)
                .isNull(Credential::getLastUsedTime);
        Long expiredCount = credentialMapper.selectCount(expiredWrapper);

        // 密码强度分布
        List<Map<String, Object>> strengthDistribution = calculateStrengthDistribution(userId);

        // 网站分类统计
        List<Map<String, Object>> categoryStats = calculateCategoryStats(userId);

        // 最近使用的凭证
        LambdaQueryWrapper<Credential> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(Credential::getUserId, userId)
                .isNotNull(Credential::getLastUsedTime)
                .orderByDesc(Credential::getLastUsedTime)
                .last("LIMIT 5");
        List<Credential> recentCredentials = credentialMapper.selectList(recentWrapper);

        List<Map<String, Object>> recentCredentialList = recentCredentials.stream()
                .map(credential -> {
                    Website website = websiteMapper.selectById(credential.getWebsiteId());
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("id", credential.getId());
                    item.put("websiteName", website != null ? website.getName() : "");
                    item.put("username", credential.getUsername());
                    item.put("lastUsedTime", credential.getLastUsedTime());
                    item.put("strength", credential.getStrength());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("websiteCount", websiteCount);
        result.put("credentialCount", credentialCount);
        result.put("weakPasswordCount", weakPasswordCount);
        result.put("expiredCount", expiredCount);
        result.put("strengthDistribution", strengthDistribution);
        result.put("categoryStats", categoryStats);
        result.put("recentCredentials", recentCredentialList);

        return result;
    }

    /**
     * 计算密码强度分布
     */
    private List<Map<String, Object>> calculateStrengthDistribution(Long userId) {
        List<Map<String, Object>> distribution = new java.util.ArrayList<>();

        // 非常弱 (1分)
        LambdaQueryWrapper<Credential> veryWeakWrapper = new LambdaQueryWrapper<>();
        veryWeakWrapper.eq(Credential::getUserId, userId).eq(Credential::getStrength, 1);
        Long veryWeakCount = credentialMapper.selectCount(veryWeakWrapper);

        // 弱 (2分)
        LambdaQueryWrapper<Credential> weakWrapper = new LambdaQueryWrapper<>();
        weakWrapper.eq(Credential::getUserId, userId).eq(Credential::getStrength, 2);
        Long weakCount = credentialMapper.selectCount(weakWrapper);

        // 中等 (3分)
        LambdaQueryWrapper<Credential> mediumWrapper = new LambdaQueryWrapper<>();
        mediumWrapper.eq(Credential::getUserId, userId).eq(Credential::getStrength, 3);
        Long mediumCount = credentialMapper.selectCount(mediumWrapper);

        // 强 (4分)
        LambdaQueryWrapper<Credential> strongWrapper = new LambdaQueryWrapper<>();
        strongWrapper.eq(Credential::getUserId, userId).eq(Credential::getStrength, 4);
        Long strongCount = credentialMapper.selectCount(strongWrapper);

        // 非常强 (5分)
        LambdaQueryWrapper<Credential> veryStrongWrapper = new LambdaQueryWrapper<>();
        veryStrongWrapper.eq(Credential::getUserId, userId).eq(Credential::getStrength, 5);
        Long veryStrongCount = credentialMapper.selectCount(veryStrongWrapper);

        Long total = veryWeakCount + weakCount + mediumCount + strongCount + veryStrongCount;

        Map<String, Object> veryWeakMap = new HashMap<>();
        veryWeakMap.put("label", "非常弱");
        veryWeakMap.put("count", veryWeakCount);
        veryWeakMap.put("percentage", total > 0 ? (veryWeakCount * 100 / total) : 0);
        veryWeakMap.put("color", "#f56c6c");
        distribution.add(veryWeakMap);

        Map<String, Object> weakMap = new HashMap<>();
        weakMap.put("label", "弱");
        weakMap.put("count", weakCount);
        weakMap.put("percentage", total > 0 ? (weakCount * 100 / total) : 0);
        weakMap.put("color", "#e6a23c");
        distribution.add(weakMap);

        Map<String, Object> mediumMap = new HashMap<>();
        mediumMap.put("label", "中等");
        mediumMap.put("count", mediumCount);
        mediumMap.put("percentage", total > 0 ? (mediumCount * 100 / total) : 0);
        mediumMap.put("color", "#409eff");
        distribution.add(mediumMap);

        Map<String, Object> strongMap = new HashMap<>();
        strongMap.put("label", "强");
        strongMap.put("count", strongCount);
        strongMap.put("percentage", total > 0 ? (strongCount * 100 / total) : 0);
        strongMap.put("color", "#67c23a");
        distribution.add(strongMap);

        Map<String, Object> veryStrongMap = new HashMap<>();
        veryStrongMap.put("label", "非常强");
        veryStrongMap.put("count", veryStrongCount);
        veryStrongMap.put("percentage", total > 0 ? (veryStrongCount * 100 / total) : 0);
        veryStrongMap.put("color", "#00b894");
        distribution.add(veryStrongMap);

        return distribution;
    }

    /**
     * 计算网站分类统计
     */
    private List<Map<String, Object>> calculateCategoryStats(Long userId) {
        List<Map<String, Object>> stats = new java.util.ArrayList<>();

        // 获取用户的所有网站分类
        List<String> categories = websiteMapper.selectCategories(userId);

        for (String category : categories) {
            if (category == null || category.isEmpty()) {
                continue;
            }

            // 获取该分类下的网站数量
            LambdaQueryWrapper<Website> websiteWrapper = new LambdaQueryWrapper<>();
            websiteWrapper.eq(Website::getUserId, userId).eq(Website::getCategory, category);
            Long websiteCount = websiteMapper.selectCount(websiteWrapper);

            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("name", category);
            categoryMap.put("count", websiteCount);
            stats.add(categoryMap);
        }

        // 添加未分类的网站
        LambdaQueryWrapper<Website> uncategorizedWrapper = new LambdaQueryWrapper<>();
        uncategorizedWrapper.eq(Website::getUserId, userId)
                .and(w -> w.isNull(Website::getCategory).or().eq(Website::getCategory, ""));
        Long uncategorizedCount = websiteMapper.selectCount(uncategorizedWrapper);

        if (uncategorizedCount > 0) {
            stats.add(Map.of("name", "未分类", "count", uncategorizedCount));
        }

        return stats;
    }
}
