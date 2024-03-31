package com.book_everywhere.jwt.filter;


import com.book_everywhere.jwt.dto.RefreshDto;
import com.book_everywhere.jwt.service.RefreshService;
import com.book_everywhere.jwt.token.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static com.book_everywhere.jwt.token.TokenType.REFRESH;

@RequiredArgsConstructor
@Component
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;
    private final RefreshService refreshService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isLogoutRequest(httpRequest)) {
            handleLogout(httpRequest, httpResponse);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && "/logout".equals(request.getRequestURI());
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!refreshService.리프레시토큰조회(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        RefreshDto refreshDto = refreshService.리프레시토큰객체조회(refreshToken);
        refreshService.리프레시토큰삭제(refreshDto.getUsername());
        clearRefreshTokenCookie(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH.getType().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH.getType(), null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}