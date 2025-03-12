package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures static resource handlers.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);
    }

    /**
     * Configures view controllers for SPA support.
     *
     * @param registry the view controller registry
     */
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/{path:[\\w-]+}")
                .setViewName("forward:/");
        registry.addViewController("/{path1:[\\w-]+}/{path2:[\\w-]+}")
                .setViewName("forward:/");
        registry.addViewController("/{path1:[\\w-]+}/{path2:[\\w-]+}/{path3:[\\w-]+}")
                .setViewName("forward:/");
    }
    
    /**
     * Configures CORS settings.
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * Adds security headers to all responses.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(securityHeadersInterceptor());
    }
    
    /**
     * Creates a security headers interceptor.
     *
     * @return the security headers interceptor
     */
    @Bean
    public SecurityHeadersInterceptor securityHeadersInterceptor() {
        return new SecurityHeadersInterceptor();
    }
    
    /**
     * Interceptor to add security headers to all responses.
     */
    public static class SecurityHeadersInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(
                @NonNull HttpServletRequest request, 
                @NonNull HttpServletResponse response, 
                @NonNull Object handler) {
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            return true;
        }
    }
} 