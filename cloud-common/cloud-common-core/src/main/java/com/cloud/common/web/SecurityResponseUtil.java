package com.cloud.common.web;

import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 安全过滤器中输出 JSON 响应的轻量工具（供业务服务 SecurityConfig 复用）。
 */
public final class SecurityResponseUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SecurityResponseUtil() {
    }

    public static void write(HttpServletResponse response, ResultCode code) throws IOException {
        response.setStatus(code.getCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.error(code)));
    }
}
