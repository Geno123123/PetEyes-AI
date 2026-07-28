package com.capstone.backend.auth.oauth.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class OAuthRedirectUriCaptureFilter extends OncePerRequestFilter {

    private final OAuthRedirectUriCookieSupport redirectUriCookieSupport;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.startsWith("/api/oauth2/authorization/")) {
            String redirectUri = request.getParameter("redirectUri");
            if (redirectUriCookieSupport.isAllowedRedirectUri(redirectUri)) {
                redirectUriCookieSupport.save(response, redirectUri.trim());
            }
        }
        filterChain.doFilter(request, response);
    }
}
