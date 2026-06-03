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

    @Test
    void getMyProfile_success_shouldReturnUserProfile() {
        UserProfileResponse expectedResponse = UserProfileResponse.builder()
                .id(userId)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .build();

        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));
        when(userMapper.toUserProfileResponse(user)).thenReturn(expectedResponse);

        UserProfileResponse response = userService.getMyProfile(Objects.requireNonNull(userId));

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Nguyen Van A", response.getFullName());
        assertEquals(UserRole.REQUESTER, response.getRole());

        verify(userRepository).findById(Objects.requireNonNull(userId));
        verify(userMapper).toUserProfileResponse(user);
    }

    @Test
    void getMyProfile_shouldThrowResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getMyProfile(Objects.requireNonNull(userId)));

        assertEquals("User not found", exception.getMessage());

        verify(userMapper, never()).toUserProfileResponse(any());
    }

    @Test
    void updateMyProfile_success_shouldUpdateUserInfo() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("  Nguyen Van B  ");
        request.setPhone("  0909999999  ");
        request.setAvatarUrl("  https://example.com/avatar.png  ");

        User savedUser = createUser(userId, "Nguyen Van B", "user@example.com", UserRole.REQUESTER, true);
        savedUser.setPhone("0909999999");
        savedUser.setAvatarUrl("https://example.com/avatar.png");

        UserProfileResponse expectedResponse = UserProfileResponse.builder()
                .id(userId)
                .fullName("Nguyen Van B")
                .email(savedUser.getEmail())
                .phone("0909999999")
                .avatarUrl("https://example.com/avatar.png")
                .role(UserRole.REQUESTER)
                .isActive(true)
                .build();

        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));
        when(userRepository.save(Objects.requireNonNull(user))).thenReturn(savedUser);
        when(userMapper.toUserProfileResponse(savedUser)).thenReturn(expectedResponse);

        UserProfileResponse response = userService.updateMyProfile(Objects.requireNonNull(userId), request);

        assertEquals("Nguyen Van B", response.getFullName());
        assertEquals("0909999999", response.getPhone());
        assertEquals("https://example.com/avatar.png", response.getAvatarUrl());

        assertEquals("Nguyen Van B", user.getFullName());
        assertEquals("0909999999", user.getPhone());
        assertEquals("https://example.com/avatar.png", user.getAvatarUrl());

        verify(userRepository).save(Objects.requireNonNull(user));
    }

    @SuppressWarnings("null")
    @Test
    void updateMyProfile_shouldThrowBadRequestException_whenNoFieldProvided() {
        UpdateProfileRequest request = new UpdateProfileRequest();

        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateMyProfile(Objects.requireNonNull(userId), request));

        assertEquals("At least one field must be provided for update", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void updateMyProfile_shouldThrowBadRequestException_whenFullNameBlank() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("   ");

        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateMyProfile(Objects.requireNonNull(userId), request));

        assertEquals("Full name must not be blank", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_success_shouldReturnUserDetail() {
        UserDetailResponse expectedResponse = UserDetailResponse.builder()
                .id(userId)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();

        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));
        when(userMapper.toUserDetailResponse(user)).thenReturn(expectedResponse);

        UserDetailResponse response = userService.getUserById(Objects.requireNonNull(userId));

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Nguyen Van A", response.getFullName());

        verify(userRepository).findById(Objects.requireNonNull(userId));
        verify(userMapper).toUserDetailResponse(user);
    }

    @Test
    void getUserById_shouldThrowResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(Objects.requireNonNull(userId)));

        assertEquals("User not found", exception.getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void getUsers_success_shouldReturnAllUsers_whenNoFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        UserSummaryResponse summary = UserSummaryResponse.builder()
                .id(userId)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserSummaryResponse(user)).thenReturn(summary);

        Page<UserSummaryResponse> response = userService.getUsers(null, null, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(userId, response.getContent().get(0).getId());

        verify(userRepository).findAll(pageable);
    }

    @SuppressWarnings("null")
    @Test
    void getUsers_success_shouldFilterByKeywordAndRole() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        UserSummaryResponse summary = UserSummaryResponse.builder()
                .id(userId)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();

        when(userRepository.findByFullNameContainingIgnoreCaseAndRole(
                "Nguyen",
                UserRole.REQUESTER,
                pageable))
                .thenReturn(userPage);

        when(userMapper.toUserSummaryResponse(user)).thenReturn(summary);

        Page<UserSummaryResponse> response = userService.getUsers(
                "  Nguyen  ",
                UserRole.REQUESTER,
                pageable);

        assertEquals(1, response.getTotalElements());

        verify(userRepository).findByFullNameContainingIgnoreCaseAndRole(
                "Nguyen",
                UserRole.REQUESTER,
                pageable);
    }

    @Test
    void updateUserRole_success_shouldUpdateRole() {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.VOLUNTEER);

        User savedUser = createUser(userId, "Nguyen Van A", "user@example.com", UserRole.VOLUNTEER, true);

        UserDetailResponse expectedResponse = UserDetailResponse.builder()
                .id(userId)
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(UserRole.VOLUNTEER)
                .isActive(true)
                .build();

        when(userRepository.findById(Objects.requireNonNull(adminId))).thenReturn(Optional.of(admin));
        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));
        when(userRepository.save(Objects.requireNonNull(user))).thenReturn(savedUser);
        when(userMapper.toUserDetailResponse(savedUser)).thenReturn(expectedResponse);

        UserDetailResponse response = userService.updateUserRole(Objects.requireNonNull(userId), request,
                Objects.requireNonNull(adminId));

        assertEquals(UserRole.VOLUNTEER, response.getRole());
        assertEquals(UserRole.VOLUNTEER, user.getRole());

        verify(userRepository).save(Objects.requireNonNull(user));
    }

    @SuppressWarnings("null")
    @Test
    void updateUserRole_shouldThrowForbiddenException_whenActorIsNotAdmin() {
        UUID volunteerId = UUID.randomUUID();
        User volunteer = createUser(volunteerId, "Volunteer", "volunteer@example.com", UserRole.VOLUNTEER, true);

        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.COLLABORATOR);

        when(userRepository.findById(Objects.requireNonNull(volunteerId))).thenReturn(Optional.of(volunteer));
        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateUserRole(Objects.requireNonNull(userId), request,
                        Objects.requireNonNull(volunteerId)));

        assertEquals("Only admin can perform this action", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void updateUserRole_shouldThrowBadRequestException_whenAdminChangesOwnRoleToNonAdmin() {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.VOLUNTEER);

        when(userRepository.findById(Objects.requireNonNull(adminId))).thenReturn(Optional.of(admin));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateUserRole(Objects.requireNonNull(adminId), request,
                        Objects.requireNonNull(adminId)));

        assertEquals("Admin cannot change their own role", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void updateUserRole_shouldThrowBadRequestException_whenUserAlreadyHasRole() {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.REQUESTER);

        when(userRepository.findById(Objects.requireNonNull(adminId))).thenReturn(Optional.of(admin));
        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateUserRole(Objects.requireNonNull(userId), request,
                        Objects.requireNonNull(adminId)));

        assertEquals("User already has this role", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatus_success_shouldUpdateStatus() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setIsActive(false);

        User savedUser = createUser(userId, "Nguyen Van A", "user@example.com", UserRole.REQUESTER, false);

        UserDetailResponse expectedResponse = UserDetailResponse.builder()
                .id(userId)
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(UserRole.REQUESTER)
                .isActive(false)
                .build();

        when(userRepository.findById(Objects.requireNonNull(adminId))).thenReturn(Optional.of(admin));
        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));
        when(userRepository.save(Objects.requireNonNull(user))).thenReturn(savedUser);
        when(userMapper.toUserDetailResponse(Objects.requireNonNull(savedUser))).thenReturn(expectedResponse);

        UserDetailResponse response = userService.updateUserStatus(Objects.requireNonNull(userId), request,
                Objects.requireNonNull(adminId));

        assertFalse(response.getIsActive());
        assertFalse(user.getIsActive());

        verify(userRepository).save(Objects.requireNonNull(user));
    }

    @SuppressWarnings("null")
    @Test
    void updateUserStatus_shouldThrowBadRequestException_whenAdminDeactivatesOwnAccount() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setIsActive(false);

        when(userRepository.findById(Objects.requireNonNull(adminId))).thenReturn(Optional.of(admin));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateUserStatus(Objects.requireNonNull(adminId), request,
                        Objects.requireNonNull(adminId)));

        assertEquals("Admin cannot deactivate their own account", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void updateUserStatus_shouldThrowBadRequestException_whenStatusAlreadySame() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setIsActive(true);

        when(userRepository.findById(Objects.requireNonNull(adminId))).thenReturn(Optional.of(admin));
        when(userRepository.findById(Objects.requireNonNull(userId))).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateUserStatus(Objects.requireNonNull(userId), request,
                        Objects.requireNonNull(adminId)));

        assertEquals("User status is already set to the requested value", exception.getMessage());

        verify(userRepository, never()).save(any());
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