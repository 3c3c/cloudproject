package com.cloud.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.message.entity.MessageTemplate;

import java.util.Map;

/**
 * 消息模板服务接口
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
public interface MessageTemplateService extends IService<MessageTemplate> {

    /**
     * 根据模板编码获取模板
     *
     * @param templateCode 模板编码
     * @return 模板实体
     */
    MessageTemplate getByCode(String templateCode);

    /**
     * 渲染模板内容
     *
     * @param templateCode 模板编码
     * @param variables    模板变量
     * @return 渲染后的内容
     */
    String render(String templateCode, Map<String, Object> variables);

    /**
     * 渲染模板内容（直接传入模板对象，避免重复查库）
     *
     * @param template 模板实体
     * @param variables    模板变量
     * @return 渲染后的内容
     */
    String render(MessageTemplate template, Map<String, Object> variables);
}
