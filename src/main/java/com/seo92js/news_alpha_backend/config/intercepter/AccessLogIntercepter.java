package com.seo92js.news_alpha_backend.config.intercepter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j(topic = "ACCESS_LOGGER")
@Component
public class AccessLogIntercepter implements HandlerInterceptor {

    private static final String START_TIME_KEY = "accessLog.startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        request.setAttribute(START_TIME_KEY, System.currentTimeMillis());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

        Long startTime = (Long) request.getAttribute(START_TIME_KEY);
        long executeTime = System.currentTimeMillis() - startTime;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = "ANONYMOUS";
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userId = auth.getName();
        }

        log.info("[{}] {} | HTTP Status : {} | 소요 시간 : {}ms | IP : {} | User Id : {}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                executeTime,
                request.getRemoteAddr(),
                userId
        );
    }
}
