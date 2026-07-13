package com.cloud.file.enums;

import lombok.Getter;

/**
 * 存储类型枚举
 */
@Getter
public enum StorageType {
    MINIO("minio", "MinIO 对象存储"),
    OSS("oss", "阿里云 OSS 存储");

    private final String code;
    private final String description;

    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static StorageType fromCode(String code) {
        for (StorageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown storage type: " + code);
    }
}