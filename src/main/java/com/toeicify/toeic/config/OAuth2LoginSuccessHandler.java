package com.toeicify.toeic.config;

import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.service.CustomOauth2Service;
import com.toeicify.toeic.service.JwtService;
import com.toeicify.toeic.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * Created by hungpham on 7/8/2025
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${app.client}")
    private String client;
    private final JwtService jwtService;
    private final CustomOauth2Service customOauth2Service;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();

            String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String socialId = oAuth2User.getAttribute("sub");

            if (socialId == null) {
                socialId = oAuth2User.getAttribute("id");
            }

            User user = customOauth2Service.processOAuth2User(email, name, socialId, provider);
            CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            String redirectUrl = client + "/authentication/success?token=" + accessToken;
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            response.sendRedirect(client + "/authentication/error?isLogin=false");
        }
    }


}
