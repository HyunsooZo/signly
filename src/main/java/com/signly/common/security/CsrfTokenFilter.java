package com.signly.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CsrfTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Set CSRF token as request attribute for JSP access
            request.setAttribute("_csrf", csrfToken);
            // Set CSRF token as header for JavaScript access
            response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
        }

        filterChain.doFilter(request, response);
    }
}