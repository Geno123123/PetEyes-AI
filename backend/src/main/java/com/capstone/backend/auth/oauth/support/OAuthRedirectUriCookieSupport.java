package com.capstone.backend.auth.oauth.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OAuthRedirectUriCookieSupport {

    public static final String COOKIE_NAME = "oauth_redirect_uri";
    private static final int MAX_AGE_SECONDS = 300;

    public void save(HttpServletResponse response, String redirectUri) {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(redirectUri.getBytes(StandardCharsets.UTF_8));
        Cookie cookie = new Cookie(COOKIE_NAME, encoded);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(MAX_AGE_SECONDS);
        response.addCookie(cookie);
    }

    public Optional<String> load(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .map(this::decode)
                .filter(this::isAllowedRedirectUri)
                .findFirst();
    }

    public void clear(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public boolean isAllowedRedirectUri(String value) {
        if (value == null || value.isBlank() || value.length() > 2048) {
            return false;
        }
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            if (scheme == null || scheme.isBlank()) {
                return false;
            }
            String lowered = scheme.toLowerCase();
            return !"javascript".equals(lowered) && !"data".equals(lowered);
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    private String decode(String value) {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
