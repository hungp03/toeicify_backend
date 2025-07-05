package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.config.CustomUserDetails;
import com.toeicify.toeic.domain.User;
import com.toeicify.toeic.dto.request.AuthRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.user.UserLoginResponse;
import com.toeicify.toeic.service.AuthService;
import com.toeicify.toeic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final JwtEncoder jwtEncoder;

    @Override
    public AuthResponse login(AuthRequest request) {
        final String username = request.getUsername();
        User currentUser = userService.findByUsername(username);
        checkPasswordExists(currentUser);
        Authentication authentication = authenticate(request.getUsername(), request.getPassword());

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = generateToken(userDetails);
        UserLoginResponse.RoleResponse roleResponse = UserLoginResponse.RoleResponse.builder()
                .roleId(currentUser.getRole().getRoleId())
                .roleName(currentUser.getRole().getRoleName())
                .build();

        UserLoginResponse userLoginResponse = UserLoginResponse.builder()
                .userId(currentUser.getUserId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .fullName(currentUser.getFullName())
                .role(roleResponse)
                .build();

        return AuthResponse.builder()
                .user(userLoginResponse)
                .accessToken(token)
                .build();
    }

    private void checkPasswordExists(User user) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new BadCredentialsException("Your account has no password.");
        }
    }

    private Authentication authenticate(String username, String password) {
        return authenticationManagerBuilder.getObject().authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    private String generateToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUser().getUserId())
                .claim("username", userDetails.getUsername())
                .claim("authorities", List.of("ROLE_" + userDetails.getUser().getRole().getRoleId()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
