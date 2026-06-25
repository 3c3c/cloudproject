package com.cloud.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;        // 账号（登录标识）
    private String nickname;        // 名称（昵称/真实姓名）
    private String password;        // 密码哈希（BCrypt）
    private String mobile;          // 手机号
    private String email;           // 邮箱
    private String avatar;          // 头像 URL
    private Integer enabled;        // 状态：1 启用 0 禁用
    private Boolean mustChangePassword; // 是否下次登录强制改密
}
