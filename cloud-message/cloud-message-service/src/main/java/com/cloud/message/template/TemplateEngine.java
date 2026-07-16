package com.cloud.message.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Map;

/**
 * 模板引擎
 * 使用 FreeMarker 渲染模板内容
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
public class TemplateEngine {

    @Autowired
    private Configuration freemarkerConfiguration;

    /**
     * 渲染模板字符串
     *
     * @param templateString 模板字符串（支持变量：${code}、${expireTime}等）
     * @param variables      模板变量
     * @return 渲染后的内容
     */
    public String render(String templateString, Map<String, Object> variables) {
        try {
            // 创建内存模板
            Template template = new Template(
                "template_" + System.currentTimeMillis(),
                templateString,
                freemarkerConfiguration
            );

            // 渲染模板
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);
        } catch (Exception e) {
            log.error("模板渲染失败: templateString={}, variables={}", templateString, variables, e);
            throw new RuntimeException("模板渲染失败", e);
        }
    }

    /**
     * 渲染模板文件
     *
     * @param templateName 模板文件名称（如：sms-code.ftl）
     * @param variables    模板变量
     * @return 渲染后的内容
     */
    public String renderFile(String templateName, Map<String, Object> variables) {
        try {
            Template template = freemarkerConfiguration.getTemplate(templateName);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);
        } catch (Exception e) {
            log.error("模板文件渲染失败: templateName={}, variables={}", templateName, variables, e);
            throw new RuntimeException("模板文件渲染失败", e);
        }
    }
}
