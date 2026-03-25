package com.seo92js.news_alpha_backend.config.filter;

import com.seo92js.news_alpha_backend.common.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "ACCESS_LOGGER")
@Component
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

    private final StaticResourceProperties staticResourceProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return staticResourceProperties.isStaticResource(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {

            filterChain.doFilter(request, response);
        }
        finally {

            long executeTime = System.currentTimeMillis() - startTime;

            String maskedIp = ipMasking(request.getRemoteAddr());
            String userId = request.getRemoteUser();
            if (userId == null) userId = "ANONYMOUS";

            log.info("[{}] {} | HTTP Status : {} | 소요 시간 : {}ms | IP : {}, User Id : {}"
                    , request.getMethod(), request.getRequestURI(), response.getStatus(), executeTime, maskedIp
                    , userId);
        }
    }

    private String ipMasking(String ip) {

        if (ip == null) return "UNKNOWN";

        String[] octets = StringUtils.delimitedListToStringArray(ip, AppConstants.DOT);
        try {

            int bClassIndex = 1;
            octets[bClassIndex] = "***";

            return String.join(AppConstants.DOT, octets);
        }
        catch (ArrayIndexOutOfBoundsException e) {

            return ip;
        }
    }
}
