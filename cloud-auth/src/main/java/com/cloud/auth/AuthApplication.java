package com.cloud.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动类。
 * scanBasePackages = "com.cloud" 以便扫描 cloud-common 中的公共组件。
 */
@SpringBootApplication(scanBasePackages = "com.cloud")
@EnableDiscoveryClient
@MapperScan("com.cloud.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
