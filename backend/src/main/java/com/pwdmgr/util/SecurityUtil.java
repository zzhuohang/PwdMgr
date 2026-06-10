package com.pwdmgr.util;

import com.pwdmgr.entity.User;
import com.pwdmgr.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserMapper userMapper;

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            User user = userMapper.selectByUsername(userDetails.getUsername());
            return user != null ? user.getId() : null;
        }

        return null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return null;
    }
}
