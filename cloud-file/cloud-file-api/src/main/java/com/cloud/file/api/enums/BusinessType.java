package com.cloud.file.api.enums;

import lombok.Getter;

/**
 * 文件业务类型枚举
 */
@Getter
public enum BusinessType {

    /**
     * 用户头像
     */
    AVATAR("avatar", "用户头像"),

    /**
     * 证件照片
     */
    IDCARD("idcard", "证件照片"),

    /**
     * 通用文档
     */
    DOCUMENT("document", "通用文档"),

    /**
     * 图片文件
     */
    IMAGE("image", "图片文件"),

    /**
     * 视频文件
     */
    VIDEO("video", "视频文件"),

    /**
     * 音频文件
     */
    AUDIO("audio", "音频文件"),

    /**
     * 附件文件
     */
    ATTACHMENT("attachment", "附件文件"),

    /**
     * 合同文件
     */
    CONTRACT("contract", "合同文件"),

    /**
     * 证书文件
     */
    CERTIFICATE("certificate", "证书文件"),

    /**
     * 报告文件
     */
    REPORT("report", "报告文件"),

    /**
     * 模板文件
     */
    TEMPLATE("template", "模板文件"),

    /**
     * 日志文件
     */
    LOG("log", "日志文件"),

    /**
     * 备份文件
     */
    BACKUP("backup", "备份文件"),

    /**
     * 临时文件
     */
    TEMP("temp", "临时文件"),

    /**
     * 其他类型
     */
    OTHER("other", "其他类型");

    /**
     * 业务类型编码
     */
    private final String code;

    /**
     * 业务类型描述
     */
    private final String description;

    BusinessType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 业务类型编码
     * @return 对应的枚举值，如果不存在返回OTHER
     */
    public static BusinessType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return OTHER;
        }

        for (BusinessType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 验证编码是否有效
     *
     * @param code 业务类型编码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        for (BusinessType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
