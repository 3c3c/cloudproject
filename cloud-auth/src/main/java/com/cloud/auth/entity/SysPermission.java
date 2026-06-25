package com.cloud.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String permCode;    // 权限码，如 system-email:read 或 product:add
    private String permName;    // 说明
    private String serviceCode; // 所属产品/服务，如 product / order / system
}
