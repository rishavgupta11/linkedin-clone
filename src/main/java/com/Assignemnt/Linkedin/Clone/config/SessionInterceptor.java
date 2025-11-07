package com.Assignemnt.Linkedin.Clone.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Allow unauthenticated auth routes (login/signup)
        if (uri.startsWith("/api/auth")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("USER_EMAIL") != null) {
            return true; // user logged in — allow
        }

        // otherwise reject the request
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Not logged in\"}");
        return false;
    }
}
