package com.cloud.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型创建/更新请求DTO（支持树形结构）
 */
@Data
public class DictTypeRequest {

    @NotBlank(message = "字典类型名称不能为空")
    @Size(max = 100, message = "字典类型名称长度不能超过100个字符")
    private String dictName;

    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 50, message = "字典类型编码长度不能超过50个字符")
    private String dictCode;

    private Long parentId;

    private Integer sortOrder;

    private Integer status;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}
