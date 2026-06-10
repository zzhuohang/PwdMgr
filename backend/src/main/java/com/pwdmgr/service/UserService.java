package com.pwdmgr.service;

import com.pwdmgr.common.ResultCode;
import com.pwdmgr.entity.Credential;
import com.pwdmgr.entity.User;
import com.pwdmgr.exception.BusinessException;
import com.pwdmgr.repository.CredentialMapper;
import com.pwdmgr.repository.UserMapper;
import com.pwdmgr.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CredentialMapper credentialMapper;
    private final EncryptionUtil encryptionUtil;

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("status", user.getStatus());
        result.put("lastLoginTime", user.getLastLoginTime());
        result.put("createdAt", user.getCreatedAt());
        return result;
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改密码成功: {}", user.getUsername());
    }

    /**
     * 修改主密码
     *
     * @param userId           用户ID
     * @param oldMasterPassword 旧主密码
     * @param newMasterPassword 新主密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeMasterPassword(Long userId, String oldMasterPassword, String newMasterPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 解析存储的salt和masterKey
        String[] parts = user.getMasterKeyEncrypted().split(":");
        if (parts.length != 2) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "主密钥数据格式错误");
        }

        String oldSalt = parts[0];
        String storedMasterKey = parts[1];

        // 验证旧主密码
        String derivedKey = encryptionUtil.deriveMasterKey(oldMasterPassword, oldSalt);
        if (!derivedKey.equals(storedMasterKey)) {
            throw new BusinessException(ResultCode.MASTER_PASSWORD_ERROR);
        }

        // 生成新的salt和masterKey
        String newSalt = encryptionUtil.generateSalt();
        String newMasterKey = encryptionUtil.deriveMasterKey(newMasterPassword, newSalt);

        // 获取用户的所有凭证
        LambdaQueryWrapper<Credential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Credential::getUserId, userId);
        List<Credential> credentials = credentialMapper.selectList(wrapper);

        // 重新加密所有凭证
        for (Credential credential : credentials) {
            // 解密旧数据
            String decryptedUsername = encryptionUtil.decrypt(credential.getUsername(), storedMasterKey);
            String decryptedPassword = encryptionUtil.decrypt(credential.getPassword(), storedMasterKey);

            // 使用新密钥加密
            credential.setUsername(encryptionUtil.encrypt(decryptedUsername, newMasterKey));
            credential.setPassword(encryptionUtil.encrypt(decryptedPassword, newMasterKey));
            credential.setUpdatedAt(LocalDateTime.now());

            credentialMapper.updateById(credential);
        }

        // 更新用户的主密钥
        user.setMasterKeyEncrypted(newSalt + ":" + newMasterKey);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改主密码成功: userId={}, 重新加密凭证数量={}", userId, credentials.size());
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("更新用户状态成功: userId={}, status={}", userId, status);
    }
}
