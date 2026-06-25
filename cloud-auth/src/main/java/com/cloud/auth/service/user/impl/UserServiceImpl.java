package com.cloud.auth.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.auth.converter.UserConverter;
import com.cloud.auth.dto.user.UserRequest;
import com.cloud.auth.dto.user.UserResponse;
import com.cloud.auth.entity.SysUser;
import com.cloud.auth.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        SysUser user = userConverter.toEntity(request);
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
    public UserResponse update(Long id, UserRequest request) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        SysUser user = userConverter.toEntity(request);
        user.setId(id);

        // 如果提供了新密码，则加密更新
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
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
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        userMapper.deleteBatchIds(ids);
    }

    @Override
    public UserResponse getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            // 返回时不包含密码
            user.setPassword(null);
        }
        return userConverter.toResponse(user);
    }

    @Override
    public Page<UserResponse> page(Integer current, Integer size, String keyword) {
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
                .collect(java.util.stream.Collectors.toList()));
        return responsePage;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer enabled) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setEnabled(enabled);
        userMapper.updateById(user);
    }
}