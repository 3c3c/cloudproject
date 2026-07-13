package com.cloud.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 查询用户的角色列表 */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /** 查询用户的权限列表（聚合所有角色权限，去重） */
    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    /** 按用户名或手机号查询用户 */
    SysUser selectByUsernameOrMobile(@Param("keyword") String keyword);

    /** 绑定用户角色（注册时赋予默认角色） */
    @Insert("INSERT IGNORE INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int bindRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /** 批量绑定用户角色（分配角色时使用，INSERT IGNORE 保证幂等，忽略重复绑定） */
    @Insert("<script>" +
            "INSERT IGNORE INTO sys_user_role(user_id, role_id) VALUES " +
            "<foreach item='roleId' collection='roleIds' open='' separator=',' close=''>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    int bindRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /** 删除用户角色绑定关系（删除用户时同步删除） */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    /** 批量删除用户角色绑定关系 */
    @Delete("<script>" +
            "DELETE FROM sys_user_role WHERE user_id IN " +
            "<foreach item='item' index='index' collection='userIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    int batchDeleteUserRoles(@Param("userIds") List<Long> userIds);

    /** 根据用户ID和角色ID列表批量删除用户角色绑定关系 */
    @Delete("<script>" +
            "DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id IN " +
            "<foreach item='item' index='index' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    int deleteUserRolesByRoleIds(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
