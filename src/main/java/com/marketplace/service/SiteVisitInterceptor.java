package com.marketplace.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SiteVisitInterceptor implements HandlerInterceptor {
    private final MetricsService metricsService;

    public SiteVisitInterceptor(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            metricsService.recordSiteVisit(request);
        }
        return true;
    }
}
