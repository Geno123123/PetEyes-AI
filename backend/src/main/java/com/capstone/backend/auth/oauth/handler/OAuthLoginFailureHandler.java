package com.capstone.backend.auth.oauth.handler;

import com.capstone.backend.auth.oauth.support.OAuthRedirectUriCookieSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@lombok.RequiredArgsConstructor
public class OAuthLoginFailureHandler implements AuthenticationFailureHandler {

    private final OAuthRedirectUriCookieSupport redirectUriCookieSupport;

    @Value("${app.auth.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        log.warn("OAuth2 login failure: {}", exception.getMessage());
        String target = redirectUriCookieSupport.load(request).orElse(redirectUri)
                + "?oauthError=true"
                + "&error=" + encode(exception.getClass().getSimpleName())
                + "&message=" + encode(exception.getMessage());
        redirectUriCookieSupport.clear(response);
        response.sendRedirect(target);
    }

    private String encode(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
