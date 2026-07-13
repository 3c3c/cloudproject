package com.cloud.file.utils;

import org.apache.commons.io.FilenameUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件 Key 生成器
 * 生成格式：{businessType}/{date}/{uuid}.{ext}
 */
public class FileKeyGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    /**
     * 生成文件 Key
     *
     * @param businessType      业务类型
     * @param originalFileName 原始文件名
     * @return 文件Key
     */
    public static String generateKey(String businessType, String originalFileName) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = FilenameUtils.getExtension(originalFileName);

        // 格式：{businessType}/{date}/{uuid}.{extension}
        if (extension != null && !extension.isEmpty()) {
            return String.format("%s/%s/%s.%s", businessType, date, uuid, extension);
        } else {
            return String.format("%s/%s/%s", businessType, date, uuid);
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不包含点号，小写）
     */
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}