package com.cloud.auth.dto.user;

import lombok.Data;

/**
 * 用户创建的时候，填写密码
 */
@Data
public class UserRequest extends UserInfoRequest{

    private String password;

}