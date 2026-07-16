package com.cloud.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;

/**
 * ID 生成工具类（基于 Hutool）
 * 使用 Hutool 的雪花算法生成唯一 ID
 * // 生成单个 ID
 * long id = IdUtils.nextId();
 * // 生成指定数量的 ID
 * long[] ids = IdUtils.nextIds(10);
 *
 * @author Cloud
 */
public class IdUtils {

    /**
     * 雪花算法生成器实例（基于 Hutool）
     * 使用本地 IP 地址作为数据中心和机器标识，保证分布式环境下的唯一性
     */
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 私有构造函数，防止实例化
     */
    private IdUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 生成下一个雪花算法 ID
     *
     * @return long 类型的 ID
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 生成指定数量的雪花算法 ID
     *
     * @param count 生成 ID 的数量
     * @return ID 数组
     */
    public static long[] nextIds(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }

    /**
     * 生成雪花算法 ID 字符串
     *
     * @return ID 字符串
     */
    public static String nextIdStr() {
        return Long.toString(nextId());
    }

    /**
     * 使用自定义参数创建雪花算法生成器
     *
     * @param datacenterId 数据中心 ID (0-31)
     * @param workerId     机器 ID (0-31)
     * @return 雪花算法生成器
     */
    public static Snowflake createSnowflake(long datacenterId, long workerId) {
        return IdUtil.getSnowflake(datacenterId, workerId);
    }

    /**
     * 使用本地 IP 自动生成数据中心和机器 ID 的雪花算法生成器
     * <p>
     * 根据本地 IP 地址自动计算数据中心 ID 和机器 ID，适合分布式环境
     *
     * @return 雪花算法生成器
     */
    public static Snowflake createSnowflakeWithLocalIp() {
        // 使用本地 IP 的最后两段作为数据中心和机器 ID
        String localIp = NetUtil.getLocalhost().getHostAddress();
        String[] parts = localIp.split("\\.");
        if (parts.length == 4) {
            long datacenterId = Long.parseLong(parts[2]) & 0x1F;  // 取 IP 第3段的后5位
            long workerId = Long.parseLong(parts[3]) & 0x1F;     // 取 IP 第4段的后5位
            return IdUtil.getSnowflake(datacenterId, workerId);
        }
        // 如果获取失败，使用默认值
        return IdUtil.getSnowflake(1, 1);
    }
}
