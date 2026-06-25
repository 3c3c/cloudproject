package com.cloud.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 字典响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictResponse {

    private Long id;
    private String dictCode;
    private String dictName;
    private String dictType;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createdBy;
    private String updatedBy;
}