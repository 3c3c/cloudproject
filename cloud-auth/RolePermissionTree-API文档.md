# 根据角色查询权限树 API 文档

## 接口概述

**接口名称**: 根据角色ID查询所有权限树，并标注角色是否拥有该权限  
**接口路径**: `/auth/roles/{roleId}/permissions`  
**请求方式**: `GET`  
**接口描述**: 获取完整的权限树形结构，并在每个权限节点上标注指定角色是否拥有该权限，用于角色权限分配界面

## 请求参数

### Path Parameters

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| roleId | Long | 是 | 角色ID | 1 |

## 返回结果

### 返回结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "permCode": "system",
      "permName": "系统管理",
      "assigned": true,
      "children": [
        {
          "id": 2,
          "permCode": "user:manage",
          "permName": "用户管理",
          "assigned": true,
          "children": [
            {
              "id": 3,
              "permCode": "user:add",
              "permName": "新增用户",
              "assigned": false,
              "children": []
            },
            {
              "id": 4,
              "permCode": "user:edit",
              "permName": "编辑用户",
              "assigned": true,
              "children": []
            },
            {
              "id": 5,
              "permCode": "user:delete",
              "permName": "删除用户",
              "assigned": true,
              "children": []
            }
          ]
        },
        {
          "id": 6,
          "permCode": "role:manage",
          "permName": "角色管理",
          "assigned": false,
          "children": [
            {
              "id": 7,
              "permCode": "role:add",
              "permName": "新增角色",
              "assigned": false,
              "children": []
            },
            {
              "id": 8,
              "permCode": "role:edit",
              "permName": "编辑角色",
              "assigned": false,
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

### 返回字段说明

#### PermissionTreeWithAssignedResponse 字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 权限ID，用于标识权限节点 |
| permCode | String | 权限编码，唯一标识权限，如 "user:add" |
| permName | String | 权限名称，如 "新增用户" |
| assigned | **Boolean** | **核心字段：当前角色是否拥有该权限**<br>true=已分配给角色<br>false=未分配给角色 |
| children | Array | 子权限列表，递归结构，包含当前权限的所有下级权限 |

## 功能特性

### 1. 简化的权限树结构
- **精简字段**：只保留权限分配所需的核心字段
- **性能优化**：减少数据传输量，提升接口性能
- **聚焦核心**：专注于权限分配状态，避免冗余信息

### 2. 权限分配标记
- **assigned 字段**：每个权限节点清晰标注分配状态
- **递归标记**：所有层级的权限节点都包含 assigned 标记
- **精确标识**：准确反映角色对每个权限的拥有情况

### 3. 树形结构
- **层级完整**：保持权限的父子层级关系
- **递归构建**：自动递归构建完整树形结构
- **空节点处理**：无子节点的权限返回空数组

## 请求示例

### cURL 示例

```bash
# 查询角色ID为1的权限树
curl -X GET "http://localhost:8080/auth/roles/1/permissions" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 查询角色ID为2的权限树
curl -X GET "http://localhost:8080/auth/roles/2/permissions" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### JavaScript 示例

```javascript
// 查询角色权限树
const roleId = 1;

fetch(`http://localhost:8080/auth/roles/${roleId}/permissions`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
  .then(response => response.json())
  .then(data => {
    console.log('角色权限树:', data.data);
    
    // 遍历权限树
    data.data.forEach(permission => {
      console.log(`${permission.permName} - ${permission.assigned ? '✓已分配' : '✗未分配'}`);
      
      // 递归处理子节点
      if (permission.children && permission.children.length > 0) {
        permission.children.forEach(child => {
          console.log(`  └─ ${child.permName} - ${child.assigned ? '✓已分配' : '✗未分配'}`);
        });
      }
    });
  })
  .catch(error => {
    console.error('请求失败:', error);
  });
```

### React 示例（完整权限分配组件）

```jsx
import React, { useState, useEffect } from 'react';
import { Tree, Checkbox, Button, message } from 'antd';
import { CheckOutlined, CloseOutlined } from '@ant-design/icons';

const RolePermissionManager = ({ roleId, roleName }) => {
  const [permissionTree, setPermissionTree] = useState([]);
  const [checkedKeys, setCheckedKeys] = useState([]);
  const [loading, setLoading] = useState(false);

  // 获取角色权限树
  useEffect(() => {
    if (!roleId) return;
    
    setLoading(true);
    fetch(`/auth/roles/${roleId}/permissions`)
      .then(res => res.json())
      .then(data => {
        setPermissionTree(data.data);
        
        // 收集已分配的权限ID
        const assignedKeys = [];
        const collectAssignedKeys = (nodes) => {
          nodes.forEach(node => {
            if (node.assigned) {
              assignedKeys.push(node.id);
            }
            if (node.children && node.children.length > 0) {
              collectAssignedKeys(node.children);
            }
          });
        };
        collectAssignedKeys(data.data);
        setCheckedKeys(assignedKeys);
      })
      .catch(error => {
        message.error('获取权限失败');
        console.error('Error:', error);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [roleId]);

  // 构建树形数据
  const treeData = permissionTree.map(permission => ({
    title: (
      <span>
        {permission.permName}
        {permission.assigned && (
          <CheckOutlined style={{ color: '#52c41a', marginLeft: 8 }} />
        )}
      </span>
    ),
    key: permission.id,
    children: buildTreeChildren(permission.children)
  }));

  const buildTreeChildren = (children) => {
    if (!children || children.length === 0) return [];
    return children.map(child => ({
      title: (
        <span>
          {child.permName}
          {child.assigned && (
            <CheckOutlined style={{ color: '#52c41a', marginLeft: 8 }} />
          )}
        </span>
      ),
      key: child.id,
      children: buildTreeChildren(child.children)
    }));
  };

  // 权限选择变化
  const onCheck = (checkedKeys) => {
    setCheckedKeys(checkedKeys);
  };

  // 保存权限分配
  const handleSave = async () => {
    try {
      setLoading(true);
      // TODO: 调用权限分配接口
      // await fetch(`/auth/roles/${roleId}/permissions`, {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify({ permissionIds: checkedKeys })
      // });
      
      message.success('权限分配成功');
    } catch (error) {
      message.error('权限分配失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="role-permission-manager">
      <h3>角色权限管理 - {roleName}</h3>
      
      <Tree
        checkable
        checkedKeys={checkedKeys}
        treeData={treeData}
        onCheck={onCheck}
        loading={loading}
        defaultExpandAll
      />

      <div className="action-buttons">
        <Button 
          type="primary" 
          onClick={handleSave}
          loading={loading}
        >
          保存权限配置
        </Button>
        <Button onClick={() => setCheckedKeys([])}>
          清空选择
        </Button>
      </div>

      <div className="permission-summary">
        <p>已分配权限: {checkedKeys.length} 个</p>
        <p>总权限数: {getAllPermissionCount(permissionTree)} 个</p>
      </div>
    </div>
  );
};

// 辅助函数：统计总权限数
const getAllPermissionCount = (nodes) => {
  let count = 0;
  const countNodes = (nodeList) => {
    nodeList.forEach(node => {
      count++;
      if (node.children && node.children.length > 0) {
        countNodes(node.children);
      }
    });
  };
  countNodes(nodes);
  return count;
};

export default RolePermissionManager;
```

## 权限要求

```java
// @PreAuthorize("hasAuthority('role:query')")
```

需要 `role:query` 权限才能访问此接口（当前已注释，便于测试）

## 错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | 正常返回数据 |
| 401 | 未授权，需要登录 | 重新登录获取token |
| 403 | 无权限访问 | 联系管理员分配权限 |
| 404 | 角色不存在 | 检查角色ID是否正确 |
| 500 | 服务器内部错误 | 联系技术支持 |

### 错误响应示例

```json
{
  "code": 404,
  "message": "角色不存在",
  "data": null
}
```

## 业务逻辑说明

### 查询流程

1. **获取所有权限**：从数据库查询系统中所有权限
2. **查询角色权限**：查询指定角色已分配的权限ID列表
3. **构建权限映射**：将权限列表转换为ID到权限对象的映射
4. **递归构建树形结构**：从根节点开始递归构建权限树
5. **添加权限标记**：为每个权限节点添加 assigned 字段

### assigned 字段逻辑

```java
// 查询角色已分配的权限ID
List<Long> assignedPermissionIds = rolePermissionMapper.getPermissionIdsByRoleId(roleId);
Set<Long> assignedSet = assignedPermissionIds.stream().collect(Collectors.toSet());

// 为每个权限设置分配标记
node.setAssigned(assignedSet.contains(permission.getId()));
```

### 树形结构构建算法

```java
private List<PermissionTreeWithAssignedResponse> buildPermissionTreeWithAssigned(
    List<SysPermission> allPermissions,
    Long parentId,
    Set<Long> assignedSet) {
    
    return allPermissions.stream()
        .filter(p -> p.getParentId().equals(parentId))
        .map(permission -> {
            PermissionTreeWithAssignedResponse node = new PermissionTreeWithAssignedResponse();
            node.setId(permission.getId());
            node.setPermCode(permission.getPermCode());
            node.setPermName(permission.getPermName());
            node.setAssigned(assignedSet.contains(permission.getId()));
            
            // 递归构建子节点
            List<PermissionTreeWithAssignedResponse> children = 
                buildPermissionTreeWithAssigned(allPermissions, permission.getId(), assignedSet);
            node.setChildren(children);
            
            return node;
        })
        .collect(Collectors.toList());
}
```

## 使用场景

### 1. 角色权限分配界面
```javascript
// 在角色管理中，点击"分配权限"按钮
function assignPermissions(roleId, roleName) {
  // 打开权限分配对话框
  showModal(
    <RolePermissionManager roleId={roleId} roleName={roleName} />
  );
}
```

### 2. 权限查看功能
```javascript
// 查看某个角色拥有哪些权限
function viewRolePermissions(roleId) {
  fetch(`/auth/roles/${roleId}/permissions`)
    .then(res => res.json())
    .then(data => {
      const assigned = filterAssignedPermissions(data.data);
      displayPermissionList(assigned);
    });
}

// 过滤已分配的权限
function filterAssignedPermissions(permissions) {
  const result = [];
  permissions.forEach(p => {
    if (p.assigned) {
      result.push({
        id: p.id,
        permCode: p.permCode,
        permName: p.permName
      });
    }
    if (p.children) {
      result.push(...filterAssignedPermissions(p.children));
    }
  });
  return result;
}
```

### 3. 权限复制功能
```javascript
// 基于现有角色创建新角色时，复制权限配置
function copyPermissions(sourceRoleId, targetRoleId) {
  return fetch(`/auth/roles/${sourceRoleId}/permissions`)
    .then(res => res.json())
    .then(data => {
      const assignedIds = extractAssignedIds(data.data);
      return assignPermissionsToRole(targetRoleId, assignedIds);
    });
}

function extractAssignedIds(permissions) {
  const ids = [];
  permissions.forEach(p => {
    if (p.assigned) ids.push(p.id);
    if (p.children) {
      ids.push(...extractAssignedIds(p.children));
    }
  });
  return ids;
}
```

### 4. 权限对比功能
```javascript
// 对比两个角色的权限差异
function compareRolePermissions(roleId1, roleId2) {
  Promise.all([
    fetch(`/auth/roles/${roleId1}/permissions`).then(r => r.json()),
    fetch(`/auth/roles/${roleId2}/permissions`).then(r => r.json())
  ]).then(([data1, data2]) => {
    const permissions1 = flattenPermissions(data1.data);
    const permissions2 = flattenPermissions(data2.data);
    
    const onlyInRole1 = permissions1.filter(p => !p.assigned);
    const onlyInRole2 = permissions2.filter(p => !p.assigned);
    
    displayComparison({
      role1Only: onlyInRole1,
      role2Only: onlyInRole2,
      common: permissions1.filter(p => p.assigned)
    });
  });
}
```

## 性能优化建议

### 1. 前端缓存
```javascript
// 缓存权限树数据，避免重复请求
const permissionCache = new Map();

async function getPermissionTree(roleId) {
  if (permissionCache.has(roleId)) {
    return permissionCache.get(roleId);
  }
  
  const response = await fetch(`/auth/roles/${roleId}/permissions`);
  const data = await response.json();
  permissionCache.set(roleId, data.data);
  
  // 设置缓存过期时间（5分钟）
  setTimeout(() => permissionCache.delete(roleId), 5 * 60 * 1000);
  
  return data.data;
}
```

### 2. 虚拟滚动
```jsx
// 对于大型权限树，使用虚拟滚动优化性能
import { Tree } from 'react-virtualized';

function LargePermissionTree({ permissions }) {
  return (
    <Tree
      data={permissions}
      height={600}
      rowHeight={40}
      overscanRowCount={10}
    />
  );
}
```

### 3. 懒加载子节点
```javascript
// 按需加载子节点，提升初始加载速度
const loadData = async (node) => {
  if (node.children) {
    return;
  }
  
  // 懒加载子节点
  const children = await fetchChildren(node.id);
  node.children = buildTreeNodes(children);
};
```

## 注意事项

### 1. 数据一致性
- 权限树基于查询时刻的权限数据
- 如果权限结构发生变化，需要重新获取
- 建议在权限变更后刷新权限树

### 2. 前端处理
- 使用 Tree 组件展示权限树
- 支持父子权限联动选择
- 已分配的权限默认勾选
- 使用图标标识分配状态

### 3. 权限分配
- 此接口只用于查询，不修改权限分配
- 权限分配需要调用专门的分配接口
- 分配完成后需要重新获取权限树更新显示

### 4. 性能考虑
- 权限数量较多时，建议分页或虚拟滚动
- 前端应该缓存权限树数据
- 避免频繁调用此接口

## 相关接口

### 配套接口

1. **分配权限给角色**
   ```http
   POST /auth/roles/{roleId}/permissions
   Content-Type: application/json
   
   {
     "permissionIds": [1, 2, 3, 4, 5]
   }
   ```

2. **批量更新角色权限**
   ```http
   PUT /auth/roles/{roleId}/permissions
   Content-Type: application/json
   
   {
     "permissionIds": [1, 2, 3]
   }
   ```

3. **移除角色权限**
   ```http
   DELETE /auth/roles/{roleId}/permissions/{permissionId}
   ```

4. **查询角色信息**
   ```http
   GET /auth/roles/{roleId}
   ```

## 数据结构变更历史

### v2.0.0 (当前版本)
**简化字段，专注权限分配**
- ✅ 保留核心字段：id, permCode, permName, assigned, children
- ❌ 移除冗余字段：type, parentId, icon, path, component, visible, serviceCode, enabled, sort, remark
- 📈 性能提升：减少数据传输量约60%
- 🎯 聚焦核心：专注于权限分配状态显示

### v1.0.0
**初始版本**
- 包含完整的权限信息字段
- 支持权限类型、路由、组件等详细信息
- 适用于需要完整权限信息的场景

## 常见问题

### Q1: 为什么返回的数据不包含权限类型？
**A**: 此接口专注于权限分配场景，只包含分配所需的核心信息。如需权限类型等详细信息，可调用 `/auth/permissions/{id}` 接口。

### Q2: assigned 字段如何确定权限是否已分配？
**A**: assigned 字段基于角色-权限关联表 `sys_role_permission` 的查询结果，如果角色ID和权限ID存在关联关系则为true。

### Q3: 如何处理大量权限的性能问题？
**A**: 建议前端实现虚拟滚动、懒加载、分页加载等优化策略，同时合理使用缓存机制。

### Q4: 权限树的构建顺序是什么？
**A**: 权限树按照数据库中的 sort 字段和 ID 进行排序，确保权限显示的顺序一致性。

## 更新日志

- **v2.0.0** - 简化字段结构，只保留权限分配所需的核心字段，提升性能
- **v1.1.0** - 添加 assigned 字段，标注权限分配状态
- **v1.0.0** - 初始版本，支持查询角色权限树