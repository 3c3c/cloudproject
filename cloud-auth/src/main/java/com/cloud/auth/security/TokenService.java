package com.cloud.auth.security;

import com.cloud.auth.dto.login.LoginResponse;
import com.cloud.auth.dto.permission.PermissionTreeResponse;
import com.cloud.auth.entity.SysPermission;
import com.cloud.auth.mapper.SysUserMapper;
import com.cloud.common.constant.RedisConstants;
import com.cloud.common.jwt.JwtUtils;
import com.cloud.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Token服务：负责JWT生成和Redis存储
 * 专门为Security层提供，避免循环依赖
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final SysUserMapper userMapper;

    /**
     * 为认证用户生成token
     */
    public LoginResponse issueToken(LoginUser loginUser) {
        String token = jwtUtils.generateToken(
                loginUser.getUsername(),
                loginUser.getUserId(),
                loginUser.getAuthorities(),
                loginUser.getNickname(),
                loginUser.getMobile(),
                loginUser.getEmail(),
                loginUser.getAvatar(),
                loginUser.getMustChangePassword()
        );

        // 写入 Redis：login:token:{username} -> token
        redisTemplate.opsForValue().set(
                RedisConstants.loginTokenKey(loginUser.getUsername()),
                token,
                jwtUtils.getExpiration(),
                TimeUnit.MILLISECONDS
        );

        List<String> authorities = loginUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 获取用户的菜单树（包含目录和菜单，不包含按钮）
        List<PermissionTreeResponse> menuTree = buildUserMenuTree(loginUser.getUserId());

        return new LoginResponse(
                token,
                jwtUtils.getTokenPrefix(),
                loginUser.getUserId(),
                loginUser.getUsername(),
                loginUser.getNickname(),
                loginUser.getMobile(),
                loginUser.getEmail(),
                loginUser.getAvatar(),
                Boolean.TRUE.equals(loginUser.getMustChangePassword()),
                authorities,
                menuTree
        );
    }

    /**
     * 构建用户的菜单树（包含目录type=1和菜单type=2，不包含按钮type=3）
     */
    private List<PermissionTreeResponse> buildUserMenuTree(Long userId) {
        // 1. 获取用户的所有权限
        List<SysPermission> userPermissions = userMapper.selectPermissionsByUserId(userId);

        // 2. 过滤出目录和菜单（type=1或type=2），排除按钮(type=3)
        List<SysPermission> menuPermissions = userPermissions.stream()
                .filter(p -> p.getType() != null && (p.getType() == 1 || p.getType() == 2))
                .collect(Collectors.toList());

        // 3. 构建树形结构
        return buildTree(menuPermissions, 0L);
    }

    /**
     * 递归构建树形结构
     * @param permissions 权限列表
     * @param parentId 父节点ID
     * @return 树形结构列表
     */
    private List<PermissionTreeResponse> buildTree(List<SysPermission> permissions, Long parentId) {
        List<PermissionTreeResponse> tree = new ArrayList<>();

        for (SysPermission permission : permissions) {
            if (permission.getParentId().equals(parentId)) {
                PermissionTreeResponse node = convertToPermissionTreeResponse(permission);

                // 递归构建子节点
                List<PermissionTreeResponse> children = buildTree(permissions, permission.getId());
                node.setChildren(children);

                tree.add(node);
            }
        }

        return tree;
    }

    /**
     * 将 SysPermission 转换为 PermissionTreeResponse
     */
    private PermissionTreeResponse convertToPermissionTreeResponse(SysPermission permission) {
        PermissionTreeResponse response = new PermissionTreeResponse();
        response.setId(permission.getId());
        response.setPermCode(permission.getPermCode());
        response.setPermName(permission.getPermName());
        response.setType(permission.getType());
        response.setParentId(permission.getParentId());
        response.setIcon(permission.getIcon());
        response.setPath(permission.getPath());
        response.setComponent(permission.getComponent());
        response.setVisible(permission.getVisible());
        response.setServiceCode(permission.getServiceCode());
        response.setEnabled(permission.getEnabled());
        response.setSort(permission.getSort());
        response.setRemark(permission.getRemark());
        return response;
    }
}