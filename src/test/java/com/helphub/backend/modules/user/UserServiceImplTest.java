package com.helphub.backend.modules.user;

import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ForbiddenException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.user.dto.request.UpdateProfileRequest;
import com.helphub.backend.modules.user.dto.request.UpdateUserRoleRequest;
import com.helphub.backend.modules.user.dto.request.UpdateUserStatusRequest;
import com.helphub.backend.modules.user.dto.response.UserDetailResponse;
import com.helphub.backend.modules.user.dto.response.UserProfileResponse;
import com.helphub.backend.modules.user.dto.response.UserSummaryResponse;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private UUID adminId;
    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        user = createUser(userId, "Nguyen Van A", "user@example.com", UserRole.REQUESTER, true);
        admin = createUser(adminId, "Admin User", "admin@example.com", UserRole.ADMIN, true);
    }

    private User createUser(UUID id, String fullName, String email, UserRole role, Boolean isActive) {
        User user = new User();
        user.setId(id);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone("0909123456");
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setIsActive(isActive);
        return user;
    }
}