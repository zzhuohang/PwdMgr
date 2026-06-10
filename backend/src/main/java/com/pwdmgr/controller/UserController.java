package com.pwdmgr.controller;

import com.pwdmgr.common.Result;
import com.pwdmgr.service.UserService;
import com.pwdmgr.util.SecurityUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = securityUtil.getCurrentUserId();
        Map<String, Object> result = userService.getUserInfo(userId);
        return Result.success(result);
    }

    /**
     * 修改密码
     *
     * @param request 密码修改请求
     * @return 操作结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    /**
     * 修改主密码
     *
     * @param request 主密码修改请求
     * @return 操作结果
     */
    @PutMapping("/master-password")
    public Result<Void> changeMasterPassword(@RequestBody ChangeMasterPasswordRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        userService.changeMasterPassword(userId, request.getOldMasterPassword(), request.getNewMasterPassword());
        return Result.success("主密码修改成功", null);
    }

    /**
     * 密码修改请求
     */
    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }

    /**
     * 主密码修改请求
     */
    @Data
    public static class ChangeMasterPasswordRequest {
        private String oldMasterPassword;
        private String newMasterPassword;
    }
}
