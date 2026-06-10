package com.pwdmgr.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pwdmgr.common.Result;
import com.pwdmgr.dto.CredentialDTO;
import com.pwdmgr.entity.Credential;
import com.pwdmgr.service.AuthService;
import com.pwdmgr.service.CredentialService;
import com.pwdmgr.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 凭证控制器
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@RestController
@RequestMapping("/credentials")
@RequiredArgsConstructor
public class CredentialController {

    private final CredentialService credentialService;
    private final AuthService authService;
    private final SecurityUtil securityUtil;

    /**
     * 创建凭证
     *
     * @param masterPassword 主密码
     * @param dto            凭证信息
     * @return 凭证信息
     */
    @PostMapping
    public Result<Credential> createCredential(
            @RequestParam String masterPassword,
            @Valid @RequestBody CredentialDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        String masterKey = authService.getMasterKey(userId, masterPassword);
        Credential credential = credentialService.createCredential(userId, masterKey, dto);
        return Result.success("创建成功", credential);
    }

    /**
     * 更新凭证
     *
     * @param credentialId   凭证ID
     * @param masterPassword 主密码
     * @param dto            凭证信息
     * @return 凭证信息
     */
    @PutMapping("/{credentialId}")
    public Result<Credential> updateCredential(
            @PathVariable Long credentialId,
            @RequestParam String masterPassword,
            @Valid @RequestBody CredentialDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        String masterKey = authService.getMasterKey(userId, masterPassword);
        Credential credential = credentialService.updateCredential(userId, masterKey, credentialId, dto);
        return Result.success("更新成功", credential);
    }

    /**
     * 删除凭证
     *
     * @param credentialId 凭证ID
     * @return 操作结果
     */
    @DeleteMapping("/{credentialId}")
    public Result<Void> deleteCredential(@PathVariable Long credentialId) {
        Long userId = securityUtil.getCurrentUserId();
        credentialService.deleteCredential(userId, credentialId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取凭证详情（解密）
     *
     * @param credentialId   凭证ID
     * @param masterPassword 主密码
     * @return 凭证信息
     */
    @GetMapping("/{credentialId}")
    public Result<Map<String, Object>> getCredential(
            @PathVariable Long credentialId,
            @RequestParam String masterPassword) {
        Long userId = securityUtil.getCurrentUserId();
        String masterKey = authService.getMasterKey(userId, masterPassword);
        Map<String, Object> result = credentialService.getCredential(userId, masterKey, credentialId);
        return Result.success(result);
    }

    /**
     * 根据域名获取凭证列表（解密）
     *
     * @param domain         域名
     * @param masterPassword 主密码
     * @return 凭证列表
     */
    @GetMapping("/domain/{domain}")
    public Result<List<Map<String, Object>>> getCredentialsByDomain(
            @PathVariable String domain,
            @RequestParam String masterPassword) {
        Long userId = securityUtil.getCurrentUserId();
        String masterKey = authService.getMasterKey(userId, masterPassword);
        List<Map<String, Object>> result = credentialService.getCredentialsByDomain(userId, masterKey, domain);
        return Result.success(result);
    }

    /**
     * 分页查询凭证列表
     *
     * @param page      页码
     * @param size      每页数量
     * @param websiteId 网站ID
     * @param keyword   搜索关键词
     * @return 凭证列表
     */
    @GetMapping
    public Result<Page<Credential>> listCredentials(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long websiteId,
            @RequestParam(required = false) String keyword) {
        Long userId = securityUtil.getCurrentUserId();
        Page<Credential> result = credentialService.listCredentials(userId, page, size, websiteId, keyword);
        return Result.success(result);
    }

    /**
     * 获取凭证统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getCredentialStats() {
        Long userId = securityUtil.getCurrentUserId();
        Map<String, Object> result = credentialService.getCredentialStats(userId);
        return Result.success(result);
    }
}
