package com.cloud.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型表实体（支持树形结构）
 * 用于存储字典的大类，如：性别、学历、用户状态等
 * 支持上下级关系，形成树形字典分类体系
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型名称，如：性别、学历、用户状态
     */
    private String dictName;

    /**
     * 字典类型编码，如：gender、education、user_status
     */
    private String dictCode;

    /**
     * 父级ID，0表示根节点
     */
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;


}
