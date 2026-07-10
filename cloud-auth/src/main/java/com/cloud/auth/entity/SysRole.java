package com.cloud.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String roleCode;    // 角色编码
    private String remark;      // 角色说明（原roleName）
    private Integer enabled;    // 状态：1 启用 0 禁用

    @TableLogic(value = "0",delval = "now()")
    private Long deleted;    // 逻辑删除：0 未删除 删除之后变为时间戳
}
