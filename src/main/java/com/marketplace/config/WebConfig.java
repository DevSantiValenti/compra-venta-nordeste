package com.marketplace.config;

import com.marketplace.service.SiteVisitInterceptor;
import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AppProperties appProperties;
    private final SiteVisitInterceptor siteVisitInterceptor;

    public WebConfig(AppProperties appProperties, SiteVisitInterceptor siteVisitInterceptor) {
        this.appProperties = appProperties;
        this.siteVisitInterceptor = siteVisitInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Path.of(appProperties.upload().productsDir()).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/products/**").addResourceLocations(uploadDir);
        String avatarDir = Path.of(appProperties.upload().avatarsDir()).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/avatars/**").addResourceLocations(avatarDir);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(siteVisitInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/css/**", "/js/**", "/uploads/**", "/actuator/**");
    }
}
