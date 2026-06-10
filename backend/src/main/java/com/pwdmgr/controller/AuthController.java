package com.pwdmgr.controller;

import com.pwdmgr.common.Result;
import com.pwdmgr.dto.UserLoginDTO;
import com.pwdmgr.dto.UserRegisterDTO;
import com.pwdmgr.service.AuthService;
import com.pwdmgr.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody UserRegisterDTO dto) {
        Map<String, Object> result = authService.register(dto);
        return Result.success("注册成功", result);
    }

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO dto) {
        Map<String, Object> result = authService.login(dto);
        return Result.success("登录成功", result);
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的Token
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestParam String refreshToken) {
        Map<String, String> result = authService.refreshToken(refreshToken);
        return Result.success("刷新成功", result);
    }

    /**
     * 验证主密码并获取主密钥
     *
     * @param masterPassword 主密码
     * @return 主密钥
     */
    @PostMapping("/verify-master-password")
    public Result<String> verifyMasterPassword(@RequestParam String masterPassword) {
        Long userId = securityUtil.getCurrentUserId();
        String masterKey = authService.getMasterKey(userId, masterPassword);
        return Result.success("验证成功", masterKey);
    }
}
