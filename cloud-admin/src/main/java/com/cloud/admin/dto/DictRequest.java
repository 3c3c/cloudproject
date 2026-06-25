package com.cloud.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典创建/更新请求DTO
 */
@Data
public class DictRequest {

    @NotBlank(message = "字典编码不能为空")
    @Size(max = 50, message = "字典编码长度不能超过50个字符")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 50, message = "字典名称长度不能超过50个字符")
    private String dictName;

    @Size(max = 20, message = "字典类型长度不能超过20个字符")
    private String dictType;

    private Integer sortOrder;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}