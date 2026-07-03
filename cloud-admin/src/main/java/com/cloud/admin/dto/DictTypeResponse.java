package com.cloud.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典类型响应DTO（支持树形结构）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictTypeResponse {

    private Long id;
    private String dictName;
    private String dictCode;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private Boolean isLeaf;
}
