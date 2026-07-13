package com.cloud.auth.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 头像上传响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 头像URL
     */
    private String avatarUrl;

    public static AvatarUploadResponse of(Long userId, String avatarUrl) {
        return new AvatarUploadResponse(userId, avatarUrl);
    }
}
