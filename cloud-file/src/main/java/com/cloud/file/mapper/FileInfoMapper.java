package com.cloud.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.file.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息 Mapper
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
}