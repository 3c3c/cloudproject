package com.cloud.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关白名单配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayWhiteListProperties {

    /** 不需要校验 token 的路径前缀列表 */
    private List<String> whiteList = new ArrayList<>();
}
