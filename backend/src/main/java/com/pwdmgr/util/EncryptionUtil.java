package com.pwdmgr.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 16;

    @Value("${encryption.master-key-length}")
    private Integer masterKeyLength;

    /**
     * 生成主密钥
     *
     * @return 主密钥(Base64编码)
     */
    public String generateMasterKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(masterKeyLength * 8);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("生成主密钥失败: {}", e.getMessage());
            throw new RuntimeException("生成主密钥失败", e);
        }
    }

    /**
     * 使用主密钥加密数据
     *
     * @param data      待加密数据
     * @param masterKey 主密钥(Base64编码)
     * @return 加密后的数据(Base64编码)
     */
    public String encrypt(String data, String masterKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(masterKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 将IV和加密数据拼接
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("加密失败: {}", e.getMessage());
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 使用主密钥解密数据
     *
     * @param encryptedData 加密后的数据(Base64编码)
     * @param masterKey     主密钥(Base64编码)
     * @return 解密后的数据
     */
    public String decrypt(String encryptedData, String masterKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(masterKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] combined = Base64.getDecoder().decode(encryptedData);

            // 分离IV和加密数据
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] encrypted = new byte[combined.length - IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE);
            System.arraycopy(combined, IV_LENGTH_BYTE, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] decryptedData = cipher.doFinal(encrypted);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密失败: {}", e.getMessage());
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 使用主密码派生主密钥
     *
     * @param masterPassword 主密码
     * @param salt           盐值
     * @return 主密钥(Base64编码)
     */
    public String deriveMasterKey(String masterPassword, String salt) {
        try {
            // 使用PBKDF2派生密钥
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                    masterPassword.toCharArray(),
                    salt.getBytes(StandardCharsets.UTF_8),
                    100000,
                    masterKeyLength * 8
            );
            SecretKey secretKey = factory.generateSecret(spec);
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("派生主密钥失败: {}", e.getMessage());
            throw new RuntimeException("派生主密钥失败", e);
        }
    }

    /**
     * 生成随机盐值
     *
     * @return 盐值(Base64编码)
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 计算密码强度
     *
     * @param password 密码
     * @return 强度(1-5)
     */
    public Integer calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 1;
        }

        int score = 0;

        // 长度检查
        if (password.length() >= 8) {
            score++;
        }
        if (password.length() >= 12) {
            score++;
        }

        // 包含小写字母
        if (password.matches(".*[a-z].*")) {
            score++;
        }

        // 包含大写字母
        if (password.matches(".*[A-Z].*")) {
            score++;
        }

        // 包含数字
        if (password.matches(".*\\d.*")) {
            score++;
        }

        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            score++;
        }

        return Math.min(score, 5);
    }
}