package com.cloud.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典数据创建/更新请求DTO
 */
@Data
public class DictDataRequest {

    @NotNull(message = "字典类型ID不能为空")
    private Long dictTypeId;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过100个字符")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(max = 100, message = "字典值长度不能超过100个字符")
    private String dictValue;

    private Integer sortOrder;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}
