package com.project.alfa.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    throws Exception {
        String uuid = UUID.randomUUID().toString();
        log.info("[{}] RequestURI ===> [{}]", uuid, request.getRequestURI());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
    
}
