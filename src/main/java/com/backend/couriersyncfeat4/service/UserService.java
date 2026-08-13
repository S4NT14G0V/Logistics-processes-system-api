package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.CustomResponseEntity;
import com.backend.couriersyncfeat4.entity.RoleEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository,  RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    // TODO: make validations in each function
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
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

    public CustomResponseEntity deleteUser(Long id){
        if (!userRepository.existsById(id)) {
            return new CustomResponseEntity(false, "User with id " + id + " does not exist");
        }
        userRepository.deleteById(id);
        return new CustomResponseEntity(true, "User with id " + id + " successfully deleted");
    }

    public UserEntity findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
