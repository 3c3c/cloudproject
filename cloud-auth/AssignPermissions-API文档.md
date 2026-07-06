# 角色权限分配 API 文档

## 接口概述

**接口名称**: 为角色分配多个权限  
**接口路径**: `/auth/roles/{roleId}/permissions`  
**请求方式**: `POST`  
**接口描述**: 为指定角色批量分配权限，会完全替换角色现有权限，用于角色权限管理

## 请求参数

### Path Parameters

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| roleId | Long | 是 | 角色ID | 1 |

### Request Body

```json
{
  "permissionIds": [1, 2, 3, 5, 7, 8]
}
```

#### 字段说明

| 字段名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| permissionIds | Array[Long] | 是 | 要分配的权限ID列表，空数组表示清除所有权限 | [1, 2, 3] |

**验证规则**：
- permissionIds 不能为 null（但可以为空数组）
- 传空数组 `[]` 表示清除该角色的所有权限绑定
- 传非空数组表示分配指定的权限

## 返回结果

### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 返回字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应状态码，200表示成功 |
| message | String | 响应消息 |
| data | null | 此接口返回null |

## 请求示例

### cURL 示例

```bash
# 为角色ID=1分配多个权限
curl -X POST "http://localhost:8080/auth/roles/1/permissions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "permissionIds": [1, 2, 3, 5, 7, 8]
  }'

# 清空角色所有权限
curl -X POST "http://localhost:8080/auth/roles/1/permissions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "permissionIds": []
  }'

# 为管理员角色分配所有权限
curl -X POST "http://localhost:8080/auth/roles/10/permissions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "permissionIds": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
  }'
```

### JavaScript 示例

```javascript
// 为角色分配权限
async function assignPermissionsToRole(roleId, permissionIds) {
  try {
    const response = await fetch(`http://localhost:8080/auth/roles/${roleId}/permissions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        permissionIds: permissionIds
      })
    });

    const result = await response.json();
    
    if (result.code === 200) {
      console.log('权限分配成功');
      return true;
    } else {
      console.error('权限分配失败:', result.message);
      return false;
    }
  } catch (error) {
    console.error('请求失败:', error);
    return false;
  }
}

// 使用示例
assignPermissionsToRole(1, [1, 2, 3, 5, 7, 8]);

// 清空角色权限
assignPermissionsToRole(1, []);
```

### React 完整示例

```jsx
import React, { useState } from 'react';
import { Modal, Tree, Button, message, Spin } from 'antd';
import { CheckOutlined } from '@ant-design/icons';

const RolePermissionAssignDialog = ({ roleId, roleName, visible, onClose, onSuccess }) => {
  const [permissionTree, setPermissionTree] = useState([]);
  const [checkedKeys, setCheckedKeys] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // 获取权限树
  const fetchPermissionTree = async () => {
    if (!roleId) return;
    
    setLoading(true);
    try {
      const response = await fetch(`/auth/roles/${roleId}/permissions`);
      const result = await response.json();
      
      if (result.code === 200) {
        setPermissionTree(result.data);
        
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
        collectAssignedKeys(result.data);
        setCheckedKeys(assignedKeys);
      }
    } catch (error) {
      message.error('获取权限失败');
    } finally {
      setLoading(false);
    }
  };

  // 权限选择变化
  const onCheck = (checkedKeys) => {
    setCheckedKeys(checkedKeys);
  };

  // 保存权限分配
  const handleSave = async () => {
    if (!roleId) {
      message.error('角色ID不存在');
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch(`/auth/roles/${roleId}/permissions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          permissionIds: checkedKeys
        })
      });

      const result = await response.json();
      
      if (result.code === 200) {
        message.success('权限分配成功');
        onSuccess && onSuccess();
        onClose();
      } else {
        message.error(result.message || '权限分配失败');
      }
    } catch (error) {
      message.error('权限分配失败');
      console.error('Error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  // 构建树形数据
  const buildTreeData = (nodes) => {
    return nodes.map(node => ({
      title: (
        <span>
          {node.permName}
          {node.assigned && (
            <CheckOutlined style={{ color: '#52c41a', marginLeft: 8 }} />
          )}
        </span>
      ),
      key: node.id,
      children: node.children && node.children.length > 0 
        ? buildTreeData(node.children) 
        : []
    }));
  };

  React.useEffect(() => {
    if (visible) {
      fetchPermissionTree();
    }
  }, [visible, roleId]);

  return (
    <Modal
      title={`分配权限 - ${roleName}`}
      open={visible}
      onCancel={onClose}
      width={600}
      footer={[
        <Button key="cancel" onClick={onClose}>
          取消
        </Button>,
        <Button 
          key="clear" 
          onClick={() => setCheckedKeys([])}
        >
          清空选择
        </Button>,
        <Button 
          key="submit" 
          type="primary" 
          onClick={handleSave}
          loading={submitting}
        >
          保存
        </Button>
      ]}
    >
      <Spin spinning={loading}>
        {permissionTree.length > 0 ? (
          <Tree
            checkable
            checkedKeys={checkedKeys}
            treeData={buildTreeData(permissionTree)}
            onCheck={onCheck}
            defaultExpandAll
            height={400}
          />
        ) : (
          <div style={{ textAlign: 'center', padding: '40px 0' }}>
            暂无权限数据
          </div>
        )}
      </Spin>
    </Modal>
  );
};

// 使用示例
function RoleManagement() {
  const [dialogVisible, setDialogVisible] = useState(false);
  const [selectedRoleId, setSelectedRoleId] = useState(null);
  const [selectedRoleName, setSelectedRoleName] = useState('');

  const handleAssignPermissions = (roleId, roleName) => {
    setSelectedRoleId(roleId);
    setSelectedRoleName(roleName);
    setDialogVisible(true);
  };

  const handleSuccess = () => {
    // 刷新角色列表
    console.log('权限分配成功，刷新列表');
  };

  return (
    <div>
      <Button onClick={() => handleAssignPermissions(1, '管理员')}>
        分配权限
      </Button>

      <RolePermissionAssignDialog
        roleId={selectedRoleId}
        roleName={selectedRoleName}
        visible={dialogVisible}
        onClose={() => setDialogVisible(false)}
        onSuccess={handleSuccess}
      />
    </div>
  );
}

export default RolePermissionAssignDialog;
```

## 权限要求

```java
// @PreAuthorize("hasAuthority('role:update')")
```

需要 `role:update` 权限才能调用此接口（当前已注释，便于测试）

## 错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 分配成功 | 正常返回 |
| 400 | 请求参数错误 | 检查permissionIds是否为null |
| 401 | 未授权，需要登录 | 重新登录获取token |
| 403 | 无权限访问 | 联系管理员分配权限 |
| 404 | 角色不存在 | 检查角色ID是否正确 |
| 500 | 服务器内部错误 | 联系技术支持 |

### 错误响应示例

```json
{
  "code": 400,
  "message": "权限ID列表不能为null",
  "data": null
}
```

```json
{
  "code": 404,
  "message": "角色不存在或已删除",
  "data": null
}
```

## 业务逻辑说明

### 处理流程

1. **验证角色存在性**
   ```java
   // 检查角色是否存在且未删除
   if (!roleMapper.selectById(roleId).getDeleted().equals(0)) {
       throw new RuntimeException("角色不存在或已删除");
   }
   ```

2. **删除角色现有权限**
   ```java
   // 清空角色所有现有权限分配
   rolePermissionMapper.deleteAllByRoleId(roleId);
   ```

3. **批量插入新权限**
   ```java
   // 批量插入新的角色-权限关联
   if (permissionIds != null && !permissionIds.isEmpty()) {
       List<SysRolePermission> rolePermissions = permissionIds.stream()
           .map(permId -> {
               SysRolePermission rp = new SysRolePermission();
               rp.setRoleId(roleId);
               rp.setPermId(permId);
               return rp;
           })
           .collect(Collectors.toList());
       
       rolePermissionMapper.batchInsert(rolePermissions);
   }
   ```

### 执行策略

**完全替换模式**：
- 先删除角色所有现有权限
- 再批量插入新的权限分配
- 确保权限分配完全替换，避免权限冲突

**事务保护**：
- 整个操作在一个事务中执行
- 如果任何步骤失败，会自动回滚
- 保证数据一致性

### 数据库操作

**删除操作**：
```sql
DELETE FROM sys_role_permission WHERE role_id = #{roleId}
```

**批量插入操作**：
```sql
INSERT INTO sys_role_permission (role_id, perm_id)
VALUES
(#{roleId}, #{permId1}),
(#{roleId}, #{permId2}),
(#{roleId}, #{permId3})
```

## 使用场景

### 1. 新角色权限分配
```javascript
// 创建新角色后，为角色分配初始权限
async function createRoleWithPermissions(roleData, permissionIds) {
  // 1. 创建角色
  const role = await createRole(roleData);
  
  // 2. 分配权限
  await assignPermissionsToRole(role.id, permissionIds);
  
  console.log('角色创建并分配权限成功');
}
```

### 2. 角色权限修改
```javascript
// 修改角色权限配置
async function updateRolePermissions(roleId, newPermissionIds) {
  // 直接调用分配接口，会自动替换现有权限
  await assignPermissionsToRole(roleId, newPermissionIds);
  
  console.log('角色权限更新成功');
}
```

### 3. 权限模板应用
```javascript
// 为新创建的角色应用权限模板
const permissionTemplates = {
  admin: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  manager: [1, 2, 3, 5, 7],
  user: [1, 5]
};

async function applyPermissionTemplate(roleId, templateName) {
  const permissionIds = permissionTemplates[templateName];
  await assignPermissionsToRole(roleId, permissionIds);
}
```

### 4. 权限复制
```javascript
// 复制角色的权限配置到另一个角色
async function copyRolePermissions(sourceRoleId, targetRoleId) {
  // 1. 获取源角色的权限
  const sourcePermissions = await getRolePermissions(sourceRoleId);
  const permissionIds = sourcePermissions.map(p => p.id);
  
  // 2. 应用到目标角色
  await assignPermissionsToRole(targetRoleId, permissionIds);
}
```

## 注意事项

### 1. 完全替换特性
- 此接口会完全替换角色的现有权限
- 不支持增量添加或删除单个权限
- 每次调用都会清空角色所有权限后重新分配

### 2. 空数组处理（清除权限）
```javascript
// 传空数组会清空角色所有权限
{
  "permissionIds": []
}
```
**使用场景**：
- 撤销角色的所有权限
- 重置角色权限配置
- 临时禁用角色权限

### 3. null验证
```javascript
// permissionIds不能为null，必须传递数组
{
  "permissionIds": null  // ❌ 错误，会返回400错误
}

// 正确的空数组
{
  "permissionIds": []  // ✅ 正确，表示清除所有权限
}
```

### 3. 权限ID验证
- 系统不会验证权限ID是否有效
- 建议前端传入有效的权限ID
- 无效的权限ID会被忽略或导致数据库错误

### 4. 事务回滚
- 如果批量插入失败，整个操作会回滚
- 角色会保持原有的权限配置
- 前端应该提示用户重试

### 5. 性能考虑
- 大量权限分配时建议分批处理
- 单次建议不超过1000个权限
- 使用批量插入提升性能

### 6. 并发问题
- 同一角色同时分配权限可能导致数据不一致
- 建议前端添加防重复提交机制
- 使用loading状态防止重复点击

## 相关接口

### 配套接口

1. **查询角色权限树**
   ```http
   GET /auth/roles/{roleId}/permissions
   ```
   查询角色拥有的权限树，用于权限分配界面的初始化

2. **查询权限列表**
   ```http
   GET /auth/permissions/list
   ```
   查询所有可用权限，用于权限选择

3. **创建角色**
   ```http
   POST /auth/roles
   ```
   创建新角色，创建后可以分配权限

4. **查询角色信息**
   ```http
   GET /auth/roles/{roleId}
   ```
   查询角色基本信息

## 最佳实践

### 1. 前端实现建议

```jsx
// 完整的权限分配流程
const RolePermissionFlow = ({ roleId }) => {
  const [step, setStep] = useState(1);
  const [selectedPermissions, setSelectedPermissions] = useState([]);

  const steps = [
    { title: '选择权限', description: '选择要分配的权限' },
    { title: '确认分配', description: '确认权限分配信息' },
    { title: '完成', description: '权限分配成功' }
  ];

  const handleSubmit = async () => {
    try {
      // 显示确认对话框
      Modal.confirm({
        title: '确认分配权限',
        content: `确认为该角色分配 ${selectedPermissions.length} 个权限？`,
        onOk: async () => {
          await assignPermissionsToRole(roleId, selectedPermissions);
          setStep(3);
        }
      });
    } catch (error) {
      message.error('权限分配失败');
    }
  };

  return (
    <div>
      <Steps current={step - 1} items={steps} />
      {step === 1 && <PermissionSelector onChange={setSelectedPermissions} />}
      {step === 2 && <ConfirmPage permissions={selectedPermissions} onSubmit={handleSubmit} />}
      {step === 3 && <SuccessPage />}
    </div>
  );
};
```

### 2. 错误处理

```javascript
const assignPermissionsWithErrorHandling = async (roleId, permissionIds) => {
  try {
    // 验证参数
    if (!Array.isArray(permissionIds)) {
      throw new Error('权限ID必须是数组');
    }

    if (permissionIds.length === 0) {
      // 确认是否清空权限
      const confirmed = await confirm('确定要清空该角色的所有权限吗？');
      if (!confirmed) return;
    }

    // 调用接口
    await assignPermissionsToRole(roleId, permissionIds);
    message.success('权限分配成功');

  } catch (error) {
    if (error.response?.status === 404) {
      message.error('角色不存在');
    } else if (error.response?.status === 403) {
      message.error('无权限操作');
    } else {
      message.error('权限分配失败，请稍后重试');
    }
    console.error('Error:', error);
  }
};
```

### 3. 性能优化

```javascript
// 防抖处理，避免频繁调用
import { debounce } from 'lodash';

const debouncedAssignPermissions = debounce(async (roleId, permissionIds) => {
  await assignPermissionsToRole(roleId, permissionIds);
}, 1000);

// 使用loading状态防止重复提交
const [submitting, setSubmitting] = useState(false);

const handleAssign = async () => {
  if (submitting) return;
  
  setSubmitting(true);
  try {
    await assignPermissionsToRole(roleId, permissionIds);
  } finally {
    setSubmitting(false);
  }
};
```

## 更新日志

- **v1.0.0** - 初始版本，支持角色权限批量分配
- **v1.1.0** - 添加参数验证，支持空数组清空权限
- **v1.2.0** - 优化批量插入性能，支持大量权限分配
- **v1.3.0** - 添加事务保护，确保数据一致性