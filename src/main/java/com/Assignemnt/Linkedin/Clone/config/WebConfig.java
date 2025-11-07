package com.Assignemnt.Linkedin.Clone.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SessionInterceptor sessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Protect user-only endpoints
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/api/posts/**", "/api/profile/**", "/api/comments/**", "/api/likes/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "https://your-frontend.vercel.app",  // production frontend
                        "https://*.vercel.app",              // (optional) preview builds
                        "http://localhost:*"                 // local dev
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}
