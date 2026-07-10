package com.cloud.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 权限码，格式如：product:add
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 权限类型：1=目录，2=菜单，3=按钮/权限点
     */
    private Integer type;

    /**
     * 父级ID，0或null表示根节点
     */
    private Long parentId;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 是否可见：0=隐藏，1=显示 为什么需要这个字段
     */
    private Integer visible;

    /**
     * 所属产品/服务，如：product、order、system
     */
    private String serviceCode;

    /**
     * 状态：1=启用，0=禁用
     */
    private Integer enabled;

    /**
     * 排序字段，数值越小越靠前
     */
    private Integer sort;

    /**
     * 说明
     */
    private String remark;

    @TableLogic(value = "0",delval = "now()")
    private Long deleted;    // 逻辑删除：0 未删除 删除之后变为时间戳
}
