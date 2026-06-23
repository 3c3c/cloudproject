package com.cloud.auth.security;

import com.cloud.auth.service.UserService;
import com.cloud.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * RBAC 用户加载：按用户名/手机号查询用户，并组装角色 + 权限。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LoginUser loginUser = userService.loadLoginUserByKeyword(username);
        if (loginUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return loginUser;
    }

    /** 供手机号认证加载或自动注册用户 */
    public LoginUser loadByMobile(String mobile) {
        return userService.loadLoginUserByMobile(mobile);
    }
}
