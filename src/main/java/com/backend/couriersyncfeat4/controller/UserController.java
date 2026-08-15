package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.input.UserInput;
import com.backend.couriersyncfeat4.dto.input.UserUpdateInput;
import com.backend.couriersyncfeat4.dto.output.UserResponse;
import com.backend.couriersyncfeat4.service.UserService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('user:read:all')")
    @QueryMapping
    public List<UserResponse> findAllUsers() {
        return userService.findAllUsers();
    }

    @PreAuthorize("hasAuthority('user:read:all')")
    @QueryMapping
    public UserResponse findUserById(@Argument UUID id) {
        return userService.findById(id);
    }

    @PreAuthorize("hasAuthority('user:read:own')")
    @QueryMapping
    public UserResponse getCurrentUserData() {
        return userService.getCurrentUserData();
    }

    @PreAuthorize("hasAuthority('user:create:all')")
    @MutationMapping
    public UserResponse createUser(@Argument("input") @Valid UserInput input) {
        return userService.createUser(input);
    }

    @PreAuthorize("hasAuthority('user:update:all')")
    @MutationMapping
    public UserResponse updateUser(@Argument UUID id, @Argument("input") @Valid UserUpdateInput input) {
        return userService.updateUser(id, input);
    }

    @PreAuthorize("hasAuthority('user:update:own')")
    @MutationMapping
    public UserResponse updateCurrentUser(@Argument("input") @Valid UserUpdateInput input) {
        return userService.updateCurrentUser(input);
    }

    @PreAuthorize("hasAuthority('user:delete:all')")
    @MutationMapping
    public boolean deleteUser(@Argument UUID id) {
        return userService.deleteUser(id);
    }
}
