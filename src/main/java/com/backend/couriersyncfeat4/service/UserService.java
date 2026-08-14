package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.CustomResponseEntity;
import com.backend.couriersyncfeat4.entity.RoleEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.repository.UserRepository;
import com.backend.couriersyncfeat4.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    public UserEntity getCurrentUser() {
        return userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ApplicationException(ErrorCodes.USER_NOT_FOUND));
    }

    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity findUserById(UUID id) {
        if (id == null) {
            throw new ApplicationException(ErrorCodes.INVALID_INPUT, "User id is required");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCodes.USER_NOT_FOUND));
    }

    public UserEntity addUser(UserEntity user) {
        if (user == null || user.getName() == null || user.getEmail() == null) {
            throw new RuntimeException("Username or Email is null");
        }
        RoleEntity roleEntity = roleService.findById(user.getRoleEntity().getId());
        UserEntity userEntity = new UserEntity();
        userEntity.setName(user.getName());
        userEntity.setEmail(user.getEmail());
        userEntity.setRoleEntity(roleEntity);
        userEntity.setCreatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        return userEntity;
    }

    public CustomResponseEntity deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            return new CustomResponseEntity(false, "User with id " + id + " does not exist");
        }
        userRepository.deleteById(id);
        return new CustomResponseEntity(true, "User with id " + id + " successfully deleted");
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
}
