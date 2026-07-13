package com.cloud.file.api.dto;

import lombok.Data;

/**
 * 批量删除结果
 */
@Data
public class BatchDeleteResult {
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
}