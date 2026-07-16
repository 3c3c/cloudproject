package com.cloud.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.message.entity.MessageTemplate;
import com.cloud.message.mapper.MessageTemplateMapper;
import com.cloud.message.service.MessageTemplateService;
import com.cloud.message.template.TemplateEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 消息模板服务实现
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Service
public class MessageTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplate>
    implements MessageTemplateService {

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public MessageTemplate getByCode(String templateCode) {
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageTemplate::getTemplateCode, templateCode);
        wrapper.eq(MessageTemplate::getStatus, 1); // 只查询启用的模板
        return this.getOne(wrapper);
    }

    @Override
    public String render(String templateCode, Map<String, Object> variables) {
        try {
            // 获取模板
            MessageTemplate template = getByCode(templateCode);
            if (template == null) {
                throw new RuntimeException("模板不存在或已禁用: " + templateCode);
            }

            // 渲染模板
            return render(template, variables);
        } catch (Exception e) {
            log.error("模板渲染失败: templateCode={}", templateCode, e);
            throw new RuntimeException("模板渲染失败: " + templateCode, e);
        }
    }

    @Override
    public String render(MessageTemplate template, Map<String, Object> variables) {
        try {
            return templateEngine.render(template.getContent(), variables);
        } catch (Exception e) {
            log.error("模板渲染失败: templateCode={}", template.getTemplateCode(), e);
            throw new RuntimeException("模板渲染失败: " + template.getTemplateCode(), e);
        }
    }
}
