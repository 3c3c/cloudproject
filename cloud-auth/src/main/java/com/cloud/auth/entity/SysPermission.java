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
    private String remark;    // 说明
    private String serviceCode; // 所属产品/服务，如 product / order / system
    private Integer type;       // 权限类型：1=目录/菜单，2=按钮/权限点
    private Long parentId;      // 父级权限ID，0表示根节点
    private Integer sort;       // 排序字段，数值越小越靠前
}
