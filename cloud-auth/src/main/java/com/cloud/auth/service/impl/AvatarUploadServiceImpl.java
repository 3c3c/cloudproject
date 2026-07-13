package com.cloud.auth.service.impl;

import com.cloud.auth.entity.SysUser;
import com.cloud.auth.feign.FileFeignClient;
import com.cloud.auth.mapper.SysUserMapper;
import com.cloud.auth.service.AvatarUploadService;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 头像上传服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarUploadServiceImpl implements AvatarUploadService {

    private final FileFeignClient fileFeignClient;
    private final SysUserMapper userMapper;
    private final RestTemplate restTemplate;

    @Value("${cloud.file.service-url:http://cloud-file/file}")
    private String fileServiceUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 验证文件
        validateAvatarFile(file);

        // 2. 获取用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 3. 如果用户已有头像，先删除旧头像
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                deleteOldAvatar(user.getAvatar());
            } catch (Exception e) {
                log.warn("删除旧头像失败: userId={}, avatar={}, error={}", userId, user.getAvatar(), e.getMessage());
                // 继续执行，不阻断上传新头像
            }
        }

        // 4. 上传新头像到文件服务
        String fileUrl = uploadToStorageService(file, userId);

        // 5. 更新用户头像字段
        user.setAvatar(fileUrl);
        userMapper.updateById(user);
        return fileUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAvatar(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                deleteOldAvatar(user.getAvatar());
                user.setAvatar(null);
                userMapper.updateById(user);
                log.info("用户头像删除成功: userId={}", userId);
            } catch (Exception e) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除头像失败: " + e.getMessage());
            }
        }
    }

    /**
     * 验证头像文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "头像文件不能为空");
        }

        // 文件大小限制：5MB
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.FILE_TOO_LARGE, "头像文件大小不能超过5MB");
        }

        // 文件类型验证：只允许图片格式
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED, "只支持图片格式的头像");
        }

        // 扩展名验证
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.toLowerCase().endsWith(".jpg") &&
                 !originalFilename.toLowerCase().endsWith(".jpeg") &&
                 !originalFilename.toLowerCase().endsWith(".png") &&
                 !originalFilename.toLowerCase().endsWith(".gif"))) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED, "只支持 JPG、JPEG、PNG、GIF 格式的头像");
        }
    }

    /**
     * 上传到存储服务
     */
    private String uploadToStorageService(MultipartFile file, Long userId) {
        try {
            // 使用 RestTemplate 上传文件
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

            org.springframework.http.MediaType mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA;
            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("businessType", "avatar");
            body.add("businessId", userId);

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity =
                    new org.springframework.http.HttpEntity<>(body, headers);

            String uploadUrl = fileServiceUrl + "/upload";
            org.springframework.http.ResponseEntity<Map> response = restTemplate.postForEntity(
                    uploadUrl, requestEntity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (String) data.get("fileUrl");
            } else {
                throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "上传文件失败");
            }
        } catch (Exception e) {
            log.error("上传头像到存储服务失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "上传头像失败: " + e.getMessage());
        }
    }

    /**
     * 删除旧头像
     */
    private void deleteOldAvatar(String avatarUrl) {
        try {
            // 从 URL 中提取文件 ID
            Long fileId = extractFileIdFromUrl(avatarUrl);
            if (fileId != null) {
                fileFeignClient.deleteFile(fileId);
                log.info("删除旧头像成功: fileId={}", fileId);
            }
        } catch (Exception e) {
            log.error("删除旧头像失败: avatarUrl={}, error={}", avatarUrl, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 从头像 URL 中提取文件 ID
     */
    private Long extractFileIdFromUrl(String avatarUrl) {
        try {
            // 假设 URL 格式为: http://domain/file/download?id=123 或其他格式
            // 根据实际的文件服务 URL 格式进行调整
            Pattern pattern = Pattern.compile("id=(\\d+)");
            Matcher matcher = pattern.matcher(avatarUrl);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
            // 如果 URL 格式不同，可以根据实际情况调整解析逻辑
            log.warn("无法从头像URL中提取文件ID: {}", avatarUrl);
            return null;
        } catch (Exception e) {
            log.error("解析文件ID失败: avatarUrl={}, error={}", avatarUrl, e.getMessage());
            return null;
        }
    }
}
