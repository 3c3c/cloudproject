package com.cloud.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 字典类型树形响应DTO
 * 用于返回包含子节点的字典类型树结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictTypeTreeResponse {

    private Long id;
    private String dictName;
    private String dictCode;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private Boolean isLeaf;

    /**
     * 子节点列表
     */
    private List<DictTypeTreeResponse> children;
}
