package com.cloud.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String permCode;    // 权限码 product:add
    private String permName;    // 说明
    private String serviceCode; // 所属产品/服务，如 product / order / system
}
