package com.pwdmgr.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pwdmgr.entity.Website;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 网站Mapper接口
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Mapper
public interface WebsiteMapper extends BaseMapper<Website> {

    /**
     * 根据域名查询网站
     *
     * @param userId 用户ID
     * @param domain 域名
     * @return 网站信息
     */
    @Select("SELECT * FROM website WHERE user_id = #{userId} AND domain = #{domain} AND deleted = 0")
    Website selectByDomain(@Param("userId") Long userId, @Param("domain") String domain);

    /**
     * 检查域名是否存在
     *
     * @param userId 用户ID
     * @param domain 域名
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM website WHERE user_id = #{userId} AND domain = #{domain} AND deleted = 0")
    boolean existsByDomain(@Param("userId") Long userId, @Param("domain") String domain);

    /**
     * 获取用户的所有网站分类
     *
     * @param userId 用户ID
     * @return 分类列表
     */
    @Select("SELECT DISTINCT category FROM website WHERE user_id = #{userId} AND deleted = 0 AND category IS NOT NULL AND category != ''")
    List<String> selectCategories(@Param("userId") Long userId);
}