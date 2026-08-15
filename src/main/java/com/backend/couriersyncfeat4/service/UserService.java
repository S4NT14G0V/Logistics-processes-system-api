package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.dto.input.UserInput;
import com.backend.couriersyncfeat4.dto.output.UserResponse;
import com.backend.couriersyncfeat4.entity.RoleEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.mapper.UserMapper;
import com.backend.couriersyncfeat4.repository.UserRepository;
import com.backend.couriersyncfeat4.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserEntity getCurrentUser() {
        return userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ApplicationException(ErrorCodes.USER_NOT_FOUND));
    }

    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    public UserResponse findById(UUID id) {
        return userMapper.toResponse(getById(id));
    }

    public UserEntity getById(UUID id) {
        if (id == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "User id is required");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.USER_NOT_FOUND));
    }

    public UserResponse getCurrentUserData() {
        return userMapper.toResponse(getCurrentUser());
    }

    public UserResponse createUser(UserInput input) {
        if (userRepository.existsByEmail(input.email())) {
            throw new ApplicationException(ErrorCodes.CONFLICT, "Email already registered");
        }
        RoleEntity role = roleService.findById(input.roleId());
        UserEntity user = new UserEntity();
        user.setName(input.name());
        user.setEmail(input.email());
        user.setRoleEntity(role);
        user.setPassword(passwordEncoder.encode(input.temporaryPassword()));
        user.setEnabled(true);
        user.setChangePassword(true);
        user.setCreatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(user));
    }

    public boolean deleteUser(UUID id) {
        UserEntity current = getCurrentUser();
        if (current.getId().equals(id)) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN, "You cannot delete your own account");
        }
        if (!userRepository.existsById(id)) {
            throw new ApplicationException(ErrorCodes.USER_NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        return true;
    }

    public UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.USER_NOT_FOUND));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserEntity register(String name, String email, String encodedPassword, RoleEntity role) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setEmail(email);
        userEntity.setPassword(encodedPassword);
        userEntity.setRoleEntity(role);
        userEntity.setEnabled(true);
        userEntity.setCreatedAt(LocalDateTime.now());
        return userRepository.save(userEntity);
    }

    public void changePassword(UserEntity user, String encodedNewPassword) {
        user.setPassword(encodedNewPassword);
        user.setChangePassword(false);
        userRepository.save(user);
    }
}
