package com.capstone.backend.config;

import com.capstone.backend.auth.jwt.JwtAuthenticationFilter;
import com.capstone.backend.auth.jwt.JwtProperties;
import com.capstone.backend.auth.oauth.handler.OAuthLoginFailureHandler;
import com.capstone.backend.auth.oauth.handler.OAuthLoginSuccessHandler;
import com.capstone.backend.auth.oauth.support.OAuthRedirectUriCaptureFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuthRedirectUriCaptureFilter oAuthRedirectUriCaptureFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/error", "/login/**", "/oauth2/**", "/api/oauth2/**", "/api/login/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/api/auth/token", "/api/auth/signup", "/api/auth/login", "/api/auth/naver/url", "/api/auth/kakao/url").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger/**", "/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authorization -> authorization.baseUri("/api/oauth2/authorization"))
                        .redirectionEndpoint(redirection -> redirection.baseUri("/api/login/oauth2/code/*"))
                        .successHandler(oAuthLoginSuccessHandler)
                        .failureHandler(oAuthLoginFailureHandler)
                )
                .addFilterBefore(oAuthRedirectUriCaptureFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Spring Security의 CORS 필터는 기본적으로 ERROR 디스패치에 적용되지 않아
    // 예외 발생 시 /error 로 포워딩된 응답에 CORS 헤더가 빠지는 문제가 있다.
    // 이 빈은 모든 디스패치 타입에 CORS 헤더를 보장한다.
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC, DispatcherType.FORWARD);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
