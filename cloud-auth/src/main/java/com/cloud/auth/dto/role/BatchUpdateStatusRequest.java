package com.cloud.auth.dto.role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量更新角色状态请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateStatusRequest {

    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> ids;

    /**
     * 状态值（1启用 0禁用）
     */
    @NotNull(message = "状态值不能为空")
    private Integer enabled;
}
