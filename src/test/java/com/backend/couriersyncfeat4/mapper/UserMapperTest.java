package com.backend.couriersyncfeat4.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.backend.couriersyncfeat4.dto.output.UserResponse;
import com.backend.couriersyncfeat4.entity.RoleEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = { UserMapperImpl.class, CatalogMapperImpl.class })
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldMapUserToResponse() {
        RoleEntity role = new RoleEntity();
        role.setId(1);
        role.setName("ADMIN");
        role.setDescription("Administrator");

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRoleEntity(role);

        UserResponse response = userMapper.toResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isNotNull();
        assertThat(response.role().id()).isEqualTo(1);
        assertThat(response.role().name()).isEqualTo("ADMIN");
    }
}
