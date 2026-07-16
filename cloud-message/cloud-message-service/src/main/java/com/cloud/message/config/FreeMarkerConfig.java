package com.cloud.message.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;

/**
 * FreeMarker 配置类
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@org.springframework.context.annotation.Configuration
public class FreeMarkerConfig {

    /**
     * FreeMarker 配置
     */
    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 设置默认编码
        configuration.setDefaultEncoding("UTF-8");

        // 设置异常处理器
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // 设置数字格式（避免出现千分位逗号）
        configuration.setNumberFormat("0.######");

        // 设置布尔值格式
        configuration.setBooleanFormat("true,false");

        // 设置日期格式
        configuration.setDateFormat("yyyy-MM-dd");
        configuration.setTimeFormat("HH:mm:ss");
        configuration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        return configuration;
    }
}
