package com.cloud.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.entity.SysUser;
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

    /** 按手机号查询用户 */
    SysUser selectByMobile(@Param("mobile") String mobile);

    /** 绑定用户角色（注册时赋予默认角色） */
    @org.apache.ibatis.annotations.Insert("INSERT IGNORE INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int bindRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
