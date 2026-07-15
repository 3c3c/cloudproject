package com.cloud.file.api.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 配置（局部配置，不加 @Configuration，仅作用于声明它的 @FeignClient）。
 * 透传认证头给下游服务：Authorization + 网关透传的 X-User-*。
 */
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return this::applyAuthHeaders;
    }

    private void applyAuthHeaders(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            // 非请求线程（如异步调用）拿不到上下文，需要手动透传
            return;
        }
        HttpServletRequest request = attrs.getRequest();
        copyHeader(request, template, "Authorization");
        copyHeader(request, template, "X-User-Id");
        copyHeader(request, template, "X-Username");
    }

    private void copyHeader(HttpServletRequest request, RequestTemplate template, String name) {
        String value = request.getHeader(name);
        if (value != null) {
            template.header(name, value);
        }
    }
}
