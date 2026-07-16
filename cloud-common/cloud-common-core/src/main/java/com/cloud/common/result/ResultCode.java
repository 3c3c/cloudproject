package com.cloud.common.result;

import lombok.Getter;

/**
 * 统一响应码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或认证已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已被禁用"),
    TOKEN_INVALID(1003, "Token 无效"),
    TOKEN_EXPIRED(1004, "Token 已过期"),
    TOKEN_KICKED(1005, "Token 已失效或在别处登录"),
    FORBIDDEN_OPERATION(1006, "无权执行该操作"),

    // 字典管理相关错误码 (2000-2099)
    DICT_TYPE_CODE_EXISTS(2001, "字典类型编码已存在"),
    DICT_TYPE_NOT_FOUND(2002, "字典类型不存在"),
    DICT_TYPE_PARENT_NOT_FOUND(2003, "父节点不存在"),
    DICT_TYPE_CIRCULAR_REFERENCE(2004, "不能将节点设置为自己的子孙节点的子节点"),
    DICT_TYPE_SELF_PARENT(2005, "不能将节点设置为自己的父节点"),
    DICT_TYPE_INVALID_STATUS(2006, "状态值无效，只能是0（禁用）或1（启用）"),
    DICT_DATA_NOT_FOUND(2007, "字典数据不存在"),
    DICT_TYPE_CODE_REQUIRED(2008, "字典类型编码不能为空"),

    // 权限管理相关错误码 (2100-2199)
    PERMISSION_CODE_EXISTS(2101, "权限码已存在"),
    PERMISSION_NOT_FOUND(2102, "权限不存在"),
    PERMISSION_PARENT_NOT_FOUND(2103, "父级权限不存在"),
    PERMISSION_CIRCULAR_REFERENCE(2104, "不能将权限设置为自己的子孙权限的子权限"),
    PERMISSION_SELF_PARENT(2105, "不能将权限设置为自己的父权限"),
    PERMISSION_INVALID_STATUS(2106, "状态值无效，只能是0（禁用）或1（启用）"),
    PERMISSION_HAS_CHILDREN(2107, "该权限下有子权限，请先删除子权限"),
    PERMISSION_INVALID_TYPE(2108, "权限类型无效"),
    PERMISSION_PARENT_TYPE_INVALID(2109, "父级权限类型无效"),
    PERMISSION_ASSIGNED_TO_ROLE(2110, "该权限已分配给角色，无法删除"),
    PERMISSION_PARAM_INVALID(2111, "权限参数无效"),

    // 文件管理相关错误码 (2200-2299)
    FILE_UPLOAD_FAILED(2201, "文件上传失败"),
    FILE_NOT_FOUND(2202, "文件不存在"),
    FILE_DOWNLOAD_FAILED(2203, "文件下载失败"),
    FILE_DELETE_FAILED(2204, "文件删除失败"),
    FILE_TOO_LARGE(2205, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(2206, "不支持的文件类型"),
    FILE_CONTENT_TYPE_MISMATCH(2207, "文件内容类型不匹配"),
    INVALID_FILE_KEY(2208, "无效的文件Key"),
    FILE_ALREADY_EXISTS(2209, "文件已存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
