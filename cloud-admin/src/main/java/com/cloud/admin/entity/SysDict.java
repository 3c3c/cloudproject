package com.cloud.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统字典表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典编码，如 gender, education_level
     */
    private String dictCode;

    /**
     * 字典名称，如 性别、学历
     */
    private String dictName;

    /**
     * 字典类型，如 system, business
     */
    private String dictType;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}