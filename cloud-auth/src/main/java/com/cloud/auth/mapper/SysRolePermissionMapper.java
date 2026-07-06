package com.cloud.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.auth.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-权限关联Mapper
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 查询角色拥有的所有权限ID
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Select("SELECT perm_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> getPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有权限
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteAllByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色权限关联
     * @param rolePermissions 角色-权限关联列表
     */
    void batchInsert(@Param("rolePermissions") List<SysRolePermission> rolePermissions);
}