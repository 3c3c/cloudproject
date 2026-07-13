package com.cloud.file.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除请求
 */
@Data
public class BatchDeleteRequest {

    /**
     * 文件 ID 列表
     */
    @NotEmpty(message = "文件ID列表不能为空")
    private List<Long> fileIds;
}