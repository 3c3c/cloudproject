package com.cloud.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;    // 角色编码
    private String roleName;    // 角色名称
    private String remark;      // 说明
    private Integer enabled;    // 状态：1 启用 0 禁用

    @TableLogic
    private Integer deleted;    // 逻辑删除：0 未删除 1 已删除
}
