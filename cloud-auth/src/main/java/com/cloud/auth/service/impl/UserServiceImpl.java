package com.cloud.auth.service.impl;

import com.cloud.auth.dto.RegisterRequest;
import com.cloud.auth.dto.UserInfo;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.entity.SysRole;
import com.cloud.auth.entity.SysUser;
import com.cloud.auth.mapper.SysUserMapper;
import com.cloud.auth.service.UserService;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import com.cloud.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 默认注册角色：ROLE_USER (sys_role.id = 2) */
    private static final Long DEFAULT_USER_ROLE_ID = 2L;

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginUser loadLoginUserByKeyword(String keyword) {
        SysUser user = userMapper.selectByUsernameOrMobile(keyword);
        return user == null ? null : toLoginUser(user);
    }

    @Override
    public LoginUser loadLoginUserByMobile(String mobile) {
        SysUser user = userMapper.selectByMobile(mobile);
        return user == null ? null : toLoginUser(user);
    }

    @Override
    public SysUser register(RegisterRequest req) {
        if (userMapper.selectByUsernameOrMobile(req.getUsername()) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setMobile(StringUtils.hasText(req.getMobile()) ? req.getMobile() : null);
        user.setEnabled(1);
        userMapper.insert(user);
        // 注册即赋予默认角色 ROLE_USER
        userMapper.bindRole(user.getId(), DEFAULT_USER_ROLE_ID);
        return user;
    }

    @Override
    public UserInfo buildUserInfo(LoginUser loginUser) {
        UserInfo info = new UserInfo();
        info.setUserId(loginUser.getUserId());
        info.setUsername(loginUser.getUsername());
        info.setMobile(loginUser.getMobile());
        info.setRoles(loadRoleCodes(loginUser.getUserId()));
        info.setPermissions(loadPermCodes(loginUser.getUserId()));
        return info;
    }

    private LoginUser toLoginUser(SysUser user) {
        if (user.getEnabled() == null || user.getEnabled() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 角色：ROLE_xxx（hasRole 匹配时去掉 ROLE_ 前缀）
        for (SysRole role : userMapper.selectRolesByUserId(user.getId())) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
        }
        // 权限：perm_code（hasAuthority 直接匹配）
        for (SysPermission perm : userMapper.selectPermissionsByUserId(user.getId())) {
            authorities.add(new SimpleGrantedAuthority(perm.getPermCode()));
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), user.getMobile(), authorities);
    }

    private List<String> loadRoleCodes(Long userId) {
        return userMapper.selectRolesByUserId(userId).stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    private List<String> loadPermCodes(Long userId) {
        return userMapper.selectPermissionsByUserId(userId).stream()
                .map(SysPermission::getPermCode)
                .collect(Collectors.toList());
    }
}
