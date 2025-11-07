package com.Assignemnt.Linkedin.Clone.config;

import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConfig {

    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        // Allow JSESSIONID to be sent between Render (backend) and Vercel (frontend)
        return CookieSameSiteSupplier.ofNone();
    }
}
