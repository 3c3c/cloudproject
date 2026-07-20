package com.cloud.file.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.file.api.dto.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传单个文件
     */
    FileResponse uploadFile(MultipartFile file, String businessType, Long businessId);

    /**
     * 批量上传文件
     */
    List<FileResponse> uploadFiles(List<MultipartFile> files, String businessType, Long businessId);

    /**
     * 根据 ID 获取文件信息
     */
    FileResponse getFileById(Long id);

    /**
     * 根据 fileKey 获取文件信息
     */
    FileResponse getFileByKey(String fileKey);

    /**
     * 下载文件（流式）。
     * <p>返回底层存储的 {@link InputStream}，不将整个文件读入内存，适合大文件下载/预览。
     * <b>调用方负责消费并关闭该流</b>（Spring 的 InputStreamResource 会在响应写出后自动关闭）。</p>
     *
     * @param id 文件 ID
     * @return 文件输入流
     */
    InputStream downloadFile(Long id);

    /**
     * 获取文件预览 URL
     */
    String getPresignedUrl(Long id, Integer expireSeconds);

    /**
     * 删除文件
     */
    boolean deleteFile(Long id);

    /**
     * 批量删除文件
     */
    int batchDeleteFiles(List<Long> fileIds);

    /**
     * 分页查询文件
     */
    Page<FileResponse> pageFiles(Integer current, Integer size,
                                  String businessType);
}