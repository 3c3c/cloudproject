package com.cloud.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.converter.UserConverter;
import com.cloud.auth.dto.register.RegisterRequest;
import com.cloud.auth.dto.user.UserRequest;
import com.cloud.auth.dto.user.UserResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserConverter userConverter;

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
        user.setNickname(req.getNickname());
        user.setPassword(req.getPassword());
        user.setMobile(StringUtils.hasText(req.getMobile()) ? req.getMobile() : null);
        user.setEmail(req.getEmail());
        user.setEnabled(1);
        user.setMustChangePassword(false);
        userMapper.insert(user);
        // 注册即赋予默认角色 ROLE_USER
        userMapper.bindRole(user.getId(), DEFAULT_USER_ROLE_ID);
        return user;
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
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getPassword(), user.getMobile(), authorities);
        loginUser.setNickname(user.getNickname());
        loginUser.setEmail(user.getEmail());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setMustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()));
        return loginUser;
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

    // ========== 用户管理方法实现 ==========

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        SysUser user = userConverter.toEntity(request);
        // 创建时默认启用
        if (user.getEnabled() == null) {
            user.setEnabled(1);
        }
        userMapper.insert(user);
        // 返回时不包含密码
        user.setPassword(null);
        return userConverter.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        SysUser user = userConverter.toEntity(request);
        user.setId(id);

        // 如果提供了新密码，则加密更新
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(user.getPassword());
        } else {
            // 否则保持原密码
            user.setPassword(existing.getPassword());
        }

        userMapper.updateById(user);

        // 返回时不包含密码
        user.setPassword(null);
        return userConverter.toResponse(userMapper.selectById(id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDeleteUsers(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        userMapper.deleteBatchIds(ids);
    }

    @Override
    public UserResponse getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            // 返回时不包含密码
            user.setPassword(null);
        }
        return userConverter.toResponse(user);
    }

    @Override
    public Page<UserResponse> pageUsers(Integer current, Integer size, String keyword) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 支持按用户名或手机号查询
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getMobile, keyword));
        }

        userMapper.selectPage(page, wrapper);

        // 转换为Response DTO分页对象
        Page<UserResponse> responsePage = new Page<>(current, size, page.getTotal());
        responsePage.setRecords(page.getRecords().stream()
                .map(user -> {
                    // 返回时不包含密码
                    user.setPassword(null);
                    return userConverter.toResponse(user);
                })
                .collect(Collectors.toList()));
        return responsePage;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer enabled) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setEnabled(enabled);
        userMapper.updateById(user);
    }
}
