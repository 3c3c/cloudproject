package com.cloud.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictDataResponse {

    private Long id;
    private Long dictTypeId;
    private String dictLabel;
    private String dictValue;
    private Integer sortOrder;
    private String remark;
}
