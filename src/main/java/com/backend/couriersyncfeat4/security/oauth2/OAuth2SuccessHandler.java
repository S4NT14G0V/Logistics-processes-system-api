package com.backend.couriersyncfeat4.security.oauth2;

import com.backend.couriersyncfeat4.entity.RoleEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.service.RoleService;
import com.backend.couriersyncfeat4.service.UserService;
import com.backend.couriersyncfeat4.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RoleService roleService;

    @Value("${app.frontend.redirect-uri}")
    private String redirectUri;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        UserEntity user = userService.findUserByEmail(email);
        RoleEntity role;

        if (adminEmail != null && adminEmail.equals(email)) {
            role = roleService.findById(1); //ADMIN
        } else {
            if (user != null && user.getRoleEntity() != null) {
                role = user.getRoleEntity();
            } else {
                role = roleService.findById(5); //CUSTOMER
            }
        }

        // Generar el token JWT
        String token = jwtTokenProvider.generateToken(email, role.getName().toUpperCase());

        if (user == null){
            UserEntity userEntity = new UserEntity();
            userEntity.setName(name);
            userEntity.setEmail(email);
            userEntity.setRoleEntity(role);
            userService.addUser(userEntity);
        }

        // Construir la URI de redirección con el token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}