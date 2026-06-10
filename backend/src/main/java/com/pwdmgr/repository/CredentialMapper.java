package com.pwdmgr.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pwdmgr.entity.Credential;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 凭证Mapper接口
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Mapper
public interface CredentialMapper extends BaseMapper<Credential> {

    /**
     * 根据网站ID查询凭证列表
     *
     * @param userId    用户ID
     * @param websiteId 网站ID
     * @return 凭证列表
     */
    @Select("SELECT * FROM credential WHERE user_id = #{userId} AND website_id = #{websiteId} AND deleted = 0")
    List<Credential> selectByWebsiteId(@Param("userId") Long userId, @Param("websiteId") Long websiteId);

    /**
     * 根据域名查询凭证列表
     *
     * @param userId 用户ID
     * @param domain 域名
     * @return 凭证列表
     */
    @Select("SELECT c.* FROM credential c " +
            "INNER JOIN website w ON c.website_id = w.id " +
            "WHERE c.user_id = #{userId} AND w.domain = #{domain} AND c.deleted = 0 AND w.deleted = 0")
    List<Credential> selectByDomain(@Param("userId") Long userId, @Param("domain") String domain);
}