package com.capstone.backend.auth.oauth.handler;

import com.capstone.backend.auth.oauth.support.OAuthRedirectUriCookieSupport;
import com.capstone.backend.auth.service.AuthCodeService;
import com.capstone.backend.auth.service.SocialSignupSessionService;
import com.capstone.backend.auth.jwt.JwtTokenProvider;
import com.capstone.backend.entity.User;
import com.capstone.backend.entity.type.AuthProvider;
import com.capstone.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthCodeService authCodeService;
    private final SocialSignupSessionService socialSignupSessionService;
    private final OAuthRedirectUriCookieSupport redirectUriCookieSupport;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.auth.redirect-uri}")
    private String redirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        String resolvedRedirectUri = redirectUriCookieSupport.load(request).orElse(redirectUri);
        redirectUriCookieSupport.clear(response);

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect(buildErrorRedirectUrl(resolvedRedirectUri, "InvalidAuthentication", "OAuth2 authentication token is missing"));
            return;
        }

        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        UserInfo userInfo = switch (registrationId) {
            case "naver" -> extractNaverUserInfo(oAuth2User);
            case "kakao" -> extractKakaoUserInfo(oAuth2User);
            default -> null;
        };

        if (userInfo == null || !userInfo.isValid()) {
            log.warn(
                    "OAuth2 login failed: missing user info for provider={}, providerIdPresent={}, emailPresent={}, namePresent={}",
                    registrationId,
                    userInfo != null && userInfo.providerId() != null && !userInfo.providerId().isBlank(),
                    userInfo != null && userInfo.email() != null && !userInfo.email().isBlank(),
                    userInfo != null && userInfo.name() != null && !userInfo.name().isBlank()
            );
            response.sendRedirect(buildErrorRedirectUrl(resolvedRedirectUri, "MissingUserInfo", "Required user attributes were not provided by OAuth provider"));
            return;
        }

        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        String providerIdentity = provider.identity(userInfo.providerId());
        String normalizedEmail = userInfo.email().trim().toLowerCase();

        User existingUser = userRepository.findByEmail(normalizedEmail)
                .map(existing -> updateProvider(existing, providerIdentity, userInfo.name()))
                .orElse(null);

        if (existingUser != null) {
            String code = authCodeService.issue(existingUser);
            String accessToken = jwtTokenProvider.createAccessToken(existingUser);
            response.sendRedirect(buildLoginRedirectUrl(resolvedRedirectUri, registrationId, userInfo, code, accessToken));
            return;
        }

        String signupCode = socialSignupSessionService.issue(new SocialSignupSessionService.PendingSocialUser(
                registrationId,
                userInfo.providerId(),
                normalizedEmail,
                userInfo.name(),
                userInfo.nickname(),
                userInfo.profileImageUrl(),
                null
        ));
        response.sendRedirect(buildSignupRequiredRedirectUrl(resolvedRedirectUri, registrationId, userInfo, signupCode));
    }

    @SuppressWarnings("unchecked")
    private UserInfo extractNaverUserInfo(OAuth2User oAuth2User) {
        Object responseObj = oAuth2User.getAttributes().get("response");
        if (!(responseObj instanceof Map<?, ?> responseMap)) return null;
        Map<String, Object> attrs = (Map<String, Object>) responseMap;
        return new UserInfo(
                (String) attrs.get("id"),
                (String) attrs.get("email"),
                (String) attrs.get("name"),
                (String) attrs.get("nickname"),
                (String) attrs.get("profile_image")
        );
    }

    @SuppressWarnings("unchecked")
    private UserInfo extractKakaoUserInfo(OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();
        Object idObj = attrs.get("id");
        String providerId = idObj == null ? null : String.valueOf(idObj);

        Map<String, Object> kakaoAccount = attrs.get("kakao_account") instanceof Map<?, ?> accountMap
                ? (Map<String, Object>) accountMap : Map.of();
        Map<String, Object> profile = kakaoAccount.get("profile") instanceof Map<?, ?> profileMap
                ? (Map<String, Object>) profileMap : Map.of();

        String email = (String) kakaoAccount.get("email");
        if (email == null || email.isBlank()) {
            email = providerId == null ? null : "kakao_" + providerId + "@no-email.local";
        }

        String nickname = (String) profile.get("nickname");
        String profileImageUrl = (String) profile.get("profile_image_url");
        String name = nickname;

        return new UserInfo(
                providerId,
                email,
                name,
                nickname,
                profileImageUrl
        );
    }

    private User updateProvider(User user, String providerIdentity, String name) {
        user.setProviderId(providerIdentity);
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        return userRepository.save(user);
    }

    private String buildLoginRedirectUrl(String targetRedirectUri, String provider, UserInfo userInfo, String code, String accessToken) {
        return targetRedirectUri
                + "?signupRequired=false"
                + "&provider=" + provider
                + "&providerId=" + encode(userInfo.providerId())
                + "&email=" + encode(userInfo.email())
                + "&name=" + encode(userInfo.name())
                + "&code=" + encode(code)
                + "&accessToken=" + encode(accessToken);
    }

    private String buildSignupRequiredRedirectUrl(String targetRedirectUri, String provider, UserInfo userInfo, String signupCode) {
        return targetRedirectUri
                + "?signupRequired=true"
                + "&provider=" + provider
                + "&providerId=" + encode(userInfo.providerId())
                + "&email=" + encode(userInfo.email())
                + "&name=" + encode(userInfo.name())
                + "&nickname=" + encode(userInfo.nickname())
                + "&profileImageUrl=" + encode(userInfo.profileImageUrl())
                + "&signupCode=" + encode(signupCode);
    }

    private String buildErrorRedirectUrl(String targetRedirectUri, String error, String message) {
        return targetRedirectUri
                + "?oauthError=true"
                + "&error=" + encode(error)
                + "&message=" + encode(message);
    }

    private String encode(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record UserInfo(String providerId, String email, String name, String nickname, String profileImageUrl) {
        boolean isValid() {
            return providerId != null && !providerId.isBlank()
                    && email != null && !email.isBlank()
                    && name != null && !name.isBlank();
        }
    }
}
