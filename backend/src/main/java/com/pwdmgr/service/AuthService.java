package com.pwdmgr.service;

import com.pwdmgr.common.ResultCode;
import com.pwdmgr.dto.UserLoginDTO;
import com.pwdmgr.dto.UserRegisterDTO;
import com.pwdmgr.entity.User;
import com.pwdmgr.exception.BusinessException;
import com.pwdmgr.repository.UserMapper;
import com.pwdmgr.util.EncryptionUtil;
import com.pwdmgr.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EncryptionUtil encryptionUtil;

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 用户信息和Token
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 验证两次密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 使用主密码派生加密密钥并加密存储
        String salt = encryptionUtil.generateSalt();
        String masterKey = encryptionUtil.deriveMasterKey(dto.getMasterPassword(), salt);
        // 将salt和masterKey一起存储，用分隔符分开
        user.setMasterKeyEncrypted(salt + ":" + masterKey);

        userMapper.insert(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户注册成功: {}", user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        return result;
    }

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 用户信息和Token
     */
    public Map<String, Object> login(UserLoginDTO dto) {
        // 查询用户
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户登录成功: {}", user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        return result;
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的Token
     */
    public Map<String, String> refreshToken(String refreshToken) {
        if (!jwtUtil.canTokenBeRefreshed(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "刷新Token已过期");
        }

        String newToken = jwtUtil.refreshToken(refreshToken);

        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        return result;
    }

    /**
     * 获取用户主密钥（需要验证主密码）
     *
     * @param userId         用户ID
     * @param masterPassword 主密码
     * @return 主密钥
     */
    public String getMasterKey(Long userId, String masterPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 解析存储的salt和masterKey
        String[] parts = user.getMasterKeyEncrypted().split(":");
        if (parts.length != 2) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "主密钥数据格式错误");
        }

        String salt = parts[0];
        String storedMasterKey = parts[1];

        // 使用输入的主密码派生密钥进行验证
        String derivedKey = encryptionUtil.deriveMasterKey(masterPassword, salt);

        // 验证派生的密钥是否与存储的一致
        if (!derivedKey.equals(storedMasterKey)) {
            throw new BusinessException(ResultCode.MASTER_PASSWORD_ERROR);
        }

        return storedMasterKey;
    }
}
