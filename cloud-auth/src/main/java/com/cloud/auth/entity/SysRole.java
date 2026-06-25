package com.cloud.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;    // 角色编码
    private String roleName;    // 角色名称
    private String remark;      // 说明
    private Integer enabled;    // 状态：1 启用 0 禁用
}
