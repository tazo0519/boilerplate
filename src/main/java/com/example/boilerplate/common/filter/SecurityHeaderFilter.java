package com.example.boilerplate.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SecurityHeaderFilter extends OncePerRequestFilter {

    private static final String CSP_VALUE = "default-src 'self'; frame-ancestors 'none'";
    private static final String CACHE_CONTROL_VALUE = "no-cache, no-store, must-revalidate";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Content-Security-Policy", CSP_VALUE);
        response.setHeader("Cache-Control", CACHE_CONTROL_VALUE);

        chain.doFilter(request, response);
    }
}
