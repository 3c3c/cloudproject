package com.cloud.auth.config;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RSA加密配置类
 * 负责生成和管理RSA密钥对，提供加密解密功能
 */
@Slf4j
@Configuration
public class RSAConfiguration {

    /**
     * RSA密钥对实例
     * 在应用启动时生成，用于整个应用的生命周期
     */
    @Bean
    public RSA rsaObject() {
        log.info("正在生成RSA密钥对...");
        RSA rsa = new RSA();
        log.info("RSA密钥对生成成功");
        log.debug("公钥Base64: {}", rsa.getPublicKeyBase64());
        return rsa;
    }
}
