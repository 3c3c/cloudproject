package com.cloud.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据表实体
 * 用于存储具体字典项，如：性别下的"男"、"女"，学历下的"本科"、"硕士"等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型ID，关联sys_dict_type表
     */
    private Long dictTypeId;

    /**
     * 字典标签（显示值），如：男、女、本科、硕士
     */
    private String dictLabel;

    /**
     * 字典值（实际值），如：1、2、bachelor、master
     */
    private String dictValue;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
