package com.pwdmgr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 密码管理系统启动类
 *
 * @author zhongge
 * @since 2026-06-10
 */
@SpringBootApplication
@MapperScan("com.pwdmgr.repository")
@EnableAsync
@EnableScheduling
public class PasswordManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PasswordManagerApplication.class, args);
    }
}