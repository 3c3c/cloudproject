package com.cloud.auth.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 头像上传服务接口
 */
public interface AvatarUploadService {

    /**
     * 上传用户头像
     * 如果用户已有头像，先删除旧头像再上传新头像
     *
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 删除用户头像
     *
     * @param userId 用户ID
     */
    void deleteAvatar(Long userId);
}
