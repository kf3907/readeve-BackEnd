package com.book_everywhere.domain.auth.config;

import com.book_everywhere.domain.auth.service.CustomOAuth2UserService;
import com.book_everywhere.common.jwt.filter.JwtFilter;
import com.book_everywhere.common.jwt.token.JwtProvider;
import com.book_everywhere.common.jwt.filter.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtProvider jwtProvider;


    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 필터를 위한 Bean 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("https://*.bookeverywhere.site", "http://localhost:3000"));
        config.setAllowCredentials(true); // 크리덴셜 허용
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
//                .addFilterAfter(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JwtFilter(jwtProvider), OAuth2LoginAuthenticationFilter.class)
                .authorizeHttpRequests((authorizeRequests) -> authorizeRequests
                        .requestMatchers("/").permitAll()
                        // 테스트 관련 url
//                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/health", "/env", "/test/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/reviews").permitAll()
                        // 비회원도 볼수있는 url
                        .requestMatchers("/api/review", "/api/map", "/api/tags", "/api/data/**").permitAll()
                        // 나머지
//                        .requestMatchers("/api/**").hasAuthority("ROLE_MEMBER")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .userInfoEndpoint(userInfoEndpointConfig ->
                                        userInfoEndpointConfig.userService(customOAuth2UserService))
                                .successHandler(customSuccessHandler)
                )
        ;
        return http.build();
    }

}