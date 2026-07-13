package com.cloud.file.dto.request;

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
    private List<Long> fileIds;
}