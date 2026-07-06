# 角色删除功能增强说明

## 功能更新

### 删除角色时自动清理权限绑定

**更新内容**：
在删除角色时，系统现在会自动同步删除角色与权限的绑定关系，确保数据库的完整性和一致性。

## 技术实现

### 1. 单个角色删除

```java
@Override
@Transactional
public void delete(Long id) {
    // 1. 先删除角色与权限的绑定关系
    rolePermissionMapper.deleteAllByRoleId(id);

    // 2. MyBatis Plus的逻辑删除会自动将deleted字段设置为1
    roleMapper.deleteById(id);
}
```

**执行流程**：
1. 删除 `sys_role_permission` 表中该角色的所有权限绑定
2. 逻辑删除角色（设置 `deleted=1`）
3. 事务保护，确保两个操作都成功或都失败

### 2. 批量删除角色

```java
@Override
@Transactional
public void batchDelete(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return;
    }

    // 1. 先删除所有角色与权限的绑定关系
    for (Long roleId : ids) {
        rolePermissionMapper.deleteAllByRoleId(roleId);
    }

    // 2. 使用MyBatis Plus的批量删除方法（支持逻辑删除）
    roleMapper.deleteBatchIds(ids);
}
```

**执行流程**：
1. 遍历所有要删除的角色ID
2. 删除每个角色的权限绑定关系
3. 批量逻辑删除角色

## 数据库操作

### 执行的SQL语句

**单个角色删除**：
```sql
-- 第一步：删除权限绑定
DELETE FROM sys_role_permission WHERE role_id = ?;

-- 第二步：逻辑删除角色
UPDATE sys_role SET deleted = 1 WHERE id = ?;
```

**批量删除角色**：
```sql
-- 第一步：删除权限绑定（循环执行）
DELETE FROM sys_role_permission WHERE role_id = ?;
DELETE FROM sys_role_permission WHERE role_id = ?;
...

-- 第二步：批量逻辑删除角色
UPDATE sys_role SET deleted = 1 WHERE id IN (?, ?, ...);
```

## 业务影响

### 优点

1. **数据完整性** ✅
   - 避免孤立的权限绑定记录
   - 保持数据库引用完整性

2. **数据一致性** ✅
   - 删除角色后不会有残留的权限关联
   - 查询角色权限时不会出现脏数据

3. **存储优化** ✅
   - 及时清理无用的关联数据
   - 节省数据库存储空间

4. **查询性能** ✅
   - 减少无效的权限绑定数据
   - 提升权限查询效率

### 注意事项

1. **事务保护** ✅
   - 删除操作在事务中执行
   - 任何步骤失败都会自动回滚

2. **级联删除** ✅
   - 角色删除 → 权限绑定删除
   - 用户角色绑定不受影响（单独的关联表）

3. **数据恢复** ⚠️
   - 角色使用逻辑删除，可以恢复
   - 权限绑定使用物理删除，无法恢复
   - 恢复角色后需要重新分配权限

## 使用示例

### 删除单个角色

```bash
# 删除角色ID=1
curl -X DELETE "http://localhost:8080/auth/roles/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**后台执行**：
1. 删除角色1的所有权限绑定
2. 将角色1标记为已删除

### 批量删除角色

```bash
# 批量删除多个角色
curl -X DELETE "http://localhost:8080/auth/roles/batch" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[1, 2, 3]'
```

**后台执行**：
1. 删除角色1、2、3的所有权限绑定
2. 将角色1、2、3标记为已删除

## 相关接口文档更新

### DELETE /auth/roles/{id}

**接口说明更新**：
> 删除指定角色，同时自动删除该角色与所有权限的绑定关系

**返回示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**业务逻辑**：
1. 验证角色是否存在
2. 删除角色与权限的绑定关系
3. 逻辑删除角色（deleted=1）
4. 事务提交

### DELETE /auth/roles/batch

**接口说明更新**：
> 批量删除多个角色，同时自动删除这些角色与所有权限的绑定关系

**返回示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**业务逻辑**：
1. 验证角色列表是否为空
2. 逐个删除角色与权限的绑定关系
3. 批量逻辑删除角色
4. 事务提交

## 测试验证

### 验证步骤

1. **准备测试数据**
   ```sql
   -- 查看角色权限绑定
   SELECT * FROM sys_role_permission WHERE role_id = 1;
   
   -- 查看角色信息
   SELECT * FROM sys_role WHERE id = 1;
   ```

2. **执行删除操作**
   ```bash
   curl -X DELETE "http://localhost:8080/auth/roles/1"
   ```

3. **验证结果**
   ```sql
   -- 确认权限绑定已删除
   SELECT * FROM sys_role_permission WHERE role_id = 1;
   -- 应该返回 0 条记录
   
   -- 确认角色已逻辑删除
   SELECT * FROM sys_role WHERE id = 1;
   -- deleted 应该为 1
   ```

### 预期结果

✅ 权限绑定记录被完全删除  
✅ 角色被逻辑删除（deleted=1）  
✅ 其他角色的权限绑定不受影响  
✅ 用户角色绑定不受影响

## 数据一致性保证

### 外键约束（可选）

如果需要在数据库层面强制数据完整性，可以添加外键约束：

```sql
ALTER TABLE sys_role_permission 
ADD CONSTRAINT fk_role_permission_role 
FOREIGN KEY (role_id) REFERENCES sys_role(id) 
ON DELETE CASCADE;
```

**注意**：使用外键级联删除时，应用层的手动删除仍然保留，作为双重保险。

### 定期清理

建议定期检查并清理可能的孤立数据：

```sql
-- 查找孤立的权限绑定记录
SELECT rp.* 
FROM sys_role_permission rp
LEFT JOIN sys_role r ON rp.role_id = r.id
WHERE r.id IS NULL OR r.deleted = 1;

-- 清理孤立记录（如果存在）
DELETE FROM sys_role_permission 
WHERE role_id IN (
    SELECT id FROM sys_role WHERE deleted = 1
);
```

## 版本更新记录

### v1.1.0 (当前版本)
**新增功能**：
- ✅ 删除角色时自动删除权限绑定
- ✅ 批量删除时同步清理权限关联
- ✅ 事务保护确保数据一致性

### v1.0.0
**初始版本**：
- 删除角色时保留权限绑定
- 可能产生孤立的权限绑定记录

## FAQ

### Q1: 为什么要删除权限绑定而不是保留？
**A**: 
- 避免数据库中的孤立记录
- 保持数据完整性
- 提升查询性能
- 便于数据维护

### Q2: 如果删除失败，权限绑定会被删除吗？
**A**: 
不会。整个操作在事务中执行，如果任何步骤失败，所有操作都会回滚，权限绑定不会被删除。

### Q3: 恢复已删除的角色后，权限还能恢复吗？
**A**: 
不能。权限绑定使用物理删除，无法恢复。恢复角色后需要重新分配权限。

### Q4: 批量删除大量角色会影响性能吗？
**A**: 
会有一定影响，但通过批量操作和优化SQL可以减少影响。建议分批删除，每次不超过100个角色。

### Q5: 用户角色绑定也会被删除吗？
**A**: 
不会。只删除角色与权限的绑定关系（sys_role_permission表），不影响用户与角色的绑定关系（sys_user_role表）。