package com.cloud.auth.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量更新用户状态请求DTO
 */
@Data
public class BatchUpdateUserStatusRequest {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 状态值：1=启用，0=禁用
     */
    @NotNull(message = "状态值不能为空")
    private Integer enabled;
}
