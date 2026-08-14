package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.dto.input.LoginRequest;
import com.backend.couriersyncfeat4.dto.input.LogoutRequest;
import com.backend.couriersyncfeat4.dto.input.RefreshRequest;
import com.backend.couriersyncfeat4.dto.input.RegisterRequest;
import com.backend.couriersyncfeat4.dto.output.AuthResponse;
import com.backend.couriersyncfeat4.entity.PermissionEntity;
import com.backend.couriersyncfeat4.entity.RoleEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleService roleService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new ApplicationException(
                    ErrorCodes.CONFLICT, "Email already registered");
        }

        String roleName = (adminEmail != null && adminEmail.equals(request.email())) ? "ADMIN" : "CUSTOMER";
        RoleEntity role = roleService.findByName(roleName);

        UserEntity user = userService.register(
                request.name(), request.email(), passwordEncoder.encode(request.password()), role);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException ex) {
            throw new ApplicationException(
                    ErrorCodes.UNAUTHORIZED, "Invalid email or password");
        }

        UserEntity user = userService.findUserByEmail(request.email());
        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        UserEntity user = refreshTokenService.rotate(request.refreshToken());
        return issueTokens(user);
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private AuthResponse issueTokens(UserEntity user) {
        RoleEntity role = user.getRoleEntity();

        List<String> permissions = role.getPermissions().stream()
                .map(PermissionEntity::getName)
                .toList();

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(), role.getName().toUpperCase(), permissions);
        String refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken);
    }
}
