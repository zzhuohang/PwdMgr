package com.pwdmgr.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pwdmgr.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND deleted = 0")
    boolean existsByUsername(@Param("username") String username);
}