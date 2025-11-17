package com.aio.api.user;

import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * API工具类，提供响应处理相关方法
 */
public class ApiUtil {

    // 私有构造函数，禁止实例化
    private ApiUtil() {
        throw new AssertionError("工具类不允许实例化");
    }

    /**
     * 设置示例响应
     * @param req 原生Web请求
     * @param contentType 内容类型
     * @param example 示例响应内容
     * @throws ApiResponseException 当响应对象获取失败或IO操作异常时抛出
     */
    public static void setExampleResponse(NativeWebRequest req, String contentType, String example) {
        // 获取HttpServletResponse并检查是否为null
        HttpServletResponse res = req.getNativeResponse(HttpServletResponse.class);
        if (res == null) {
            throw new ApiResponseException("无法获取HttpServletResponse对象");
        }

        try {
            res.setCharacterEncoding("UTF-8");
            res.addHeader("Content-Type", contentType);
            res.getWriter().print(example);
        } catch (IOException e) {
            // 用具体异常包装原始异常，保留堆栈信息
            throw new ApiResponseException("设置示例响应失败", e);
        }
    }

    /**
     * 自定义异常，用于表示API响应处理相关错误
     */
    public static class ApiResponseException extends RuntimeException {
        public ApiResponseException(String message) {
            super(message);
        }

        public ApiResponseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}