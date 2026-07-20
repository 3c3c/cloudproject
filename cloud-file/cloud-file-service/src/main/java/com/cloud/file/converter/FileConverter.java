package com.cloud.file.converter;

import com.cloud.file.api.dto.FileResponse;
import com.cloud.file.entity.FileInfo;
import org.mapstruct.Mapper;

/**
 * 文件转换器
 */
@Mapper(componentModel = "spring")
public interface FileConverter {

    /**
     * FileInfo 转换为 FileResponse
     */
    FileResponse toResponse(FileInfo fileInfo);
}