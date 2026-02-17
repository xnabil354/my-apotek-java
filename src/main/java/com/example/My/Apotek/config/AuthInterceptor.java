package com.example.My.Apotek.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        if (uri.equals("/login") || uri.startsWith("/h2-console") || uri.startsWith("/css") || uri.startsWith("/js")) {
            return true;
        }

        Object user = request.getSession().getAttribute("loggedInUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
