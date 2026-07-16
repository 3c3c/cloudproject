package com.cloud.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;

/**
 * Jackson 配置类，解决序列化常见问题：
 * 1. Long 类型精度丢失问题 - 将 Long 序列化为 String
 * 2. BigDecimal 类型精度丢失问题 - 将 BigDecimal 序列化为 String
 * 3. LocalDateTime 时间格式化问题 - 支持 Java 8 时间类型
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置 ObjectMapper，将数值类型序列化为 String 防止前端 JavaScript 精度丢失
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 使用 ToStringSerializer 将 Long 类型序列化为 String（防止 JavaScript 精度丢失）
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // 将 BigDecimal 序列化为 String（保持价格、金额等精度）
            builder.serializerByType(BigDecimal.class, ToStringSerializer.instance);

            // 注册 JavaTimeModule 支持 LocalDateTime 等时间类型
            builder.modulesToInstall(new JavaTimeModule());
        };
    }

    /**
     * 配置 ObjectMapper Bean
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 注册 JavaTimeModule 支持 Java 8 时间 API
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
