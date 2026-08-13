package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.entity.CustomResponseEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @QueryMapping
    public List<UserEntity> findAllUsers() {
        return userService.findAllUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @QueryMapping
    public UserEntity findUserById(@Argument Long id) {
        return userService.findUserById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @MutationMapping
    public UserEntity addUser(@Argument UserEntity user) {
        return userService.addUser(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @MutationMapping
    public CustomResponseEntity deleteUser(@Argument Long id){
        return userService.deleteUser(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR', 'CUSTOMER')")
    @QueryMapping
    public String currentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "No autenticado";
        }

        String username = authentication.getName();

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return "User: " + username + ", Roles: " + roles;
    }

}
