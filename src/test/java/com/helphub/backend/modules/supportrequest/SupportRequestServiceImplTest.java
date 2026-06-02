package com.helphub.backend.modules.supportrequest;

import com.helphub.backend.common.enums.SupportRequestStatus;
import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ForbiddenException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.supportrequest.dto.request.AssignSupportRequestToSupportLocationRequest;
import com.helphub.backend.modules.supportrequest.dto.request.CreateSupportRequestRequest;
import com.helphub.backend.modules.supportrequest.dto.request.RejectSupportRequestRequest;
import com.helphub.backend.modules.supportrequest.dto.request.UpdateSupportRequestRequest;
import com.helphub.backend.modules.supportrequest.dto.response.SupportRequestDetailResponse;
import com.helphub.backend.modules.supportrequest.dto.response.SupportRequestSummaryResponse;
import com.helphub.backend.persistence.entity.Category;
import com.helphub.backend.persistence.entity.SupportLocation;
import com.helphub.backend.persistence.entity.SupportRequest;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.CategoryRepository;
import com.helphub.backend.persistence.repository.SupportLocationRepository;
import com.helphub.backend.persistence.repository.SupportRequestRepository;
import com.helphub.backend.persistence.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportRequestServiceImplTest {

        @Mock
        private SupportRequestRepository supportRequestRepository;

        @Mock
        private SupportLocationRepository supportLocationRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SupportRequestMapper supportRequestMapper;

        @InjectMocks
        private SupportRequestServiceImpl supportRequestService;

        private UUID requesterId;
        private UUID reviewerId;
        private UUID categoryId;
        private UUID supportRequestId;
        private UUID supportLocationId;

        private User requester;
        private User reviewer;
        private User volunteer;
        private Category category;
        private SupportRequest supportRequest;
        private SupportLocation supportLocation;

        @BeforeEach
        void setUp() {
                requesterId = UUID.randomUUID();
                reviewerId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                supportRequestId = UUID.randomUUID();
                supportLocationId = UUID.randomUUID();

                requester = createUser(requesterId, "Requester", UserRole.REQUESTER);
                reviewer = createUser(reviewerId, "Reviewer", UserRole.ADMIN);
                volunteer = createUser(UUID.randomUUID(), "Volunteer", UserRole.VOLUNTEER);

                category = createCategory(categoryId, "Medical", "MEDICAL", true);
                supportLocation = createSupportLocation(supportLocationId, "Main Support Location", true);

                supportRequest = createSupportRequest(
                                supportRequestId,
                                "Need medical support",
                                "Need medicine",
                                SupportRequestStatus.PENDING,
                                requester,
                                category);
        }

        @Test
        void createSupportRequest_success_shouldCreatePendingSupportRequest() {
                CreateSupportRequestRequest request = createCreateRequest();

                SupportRequestDetailResponse expectedResponse = createDetailResponse(
                                supportRequestId,
                                SupportRequestStatus.PENDING);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
                when(supportRequestRepository.save(any(SupportRequest.class)))
                                .thenAnswer(invocation -> {
                                        SupportRequest saved = invocation.getArgument(0);
                                        saved.setId(supportRequestId);
                                        return saved;
                                });
                when(supportRequestMapper.toDetailResponse(any(SupportRequest.class)))
                                .thenReturn(expectedResponse);

                SupportRequestDetailResponse response = supportRequestService.createSupportRequest(requesterId,
                                request);

                assertNotNull(response);
                assertEquals(supportRequestId, response.getId());
                assertEquals(SupportRequestStatus.PENDING, response.getStatus());

                verify(userRepository).findById(Objects.requireNonNull(requesterId));
                verify(categoryRepository).findById(Objects.requireNonNull(categoryId));
                verify(supportRequestRepository).save(any(SupportRequest.class));
        }

        @Test
        void createSupportRequest_shouldThrowForbiddenException_whenUserIsNotRequester() {
                CreateSupportRequestRequest request = createCreateRequest();

                when(userRepository.findById(volunteer.getId())).thenReturn(Optional.of(volunteer));

                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> supportRequestService.createSupportRequest(volunteer.getId(), request));

                assertEquals("Only requester can create support request", exception.getMessage());

                verify(categoryRepository, never()).findById(Objects.requireNonNull(categoryId));
                verify(supportRequestRepository, never()).save(Objects.requireNonNull(supportRequest));
        }

        @Test
        void createSupportRequest_shouldThrowBadRequestException_whenCategoryInactive() {
                CreateSupportRequestRequest request = createCreateRequest();
                category.setIsActive(false);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportRequestService.createSupportRequest(requesterId, request));

                assertEquals("Category is inactive", exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void createSupportRequest_shouldThrowResourceNotFoundException_whenCategoryNotFound() {
                CreateSupportRequestRequest request = createCreateRequest();

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> supportRequestService.createSupportRequest(requesterId, request));

                assertEquals("Category not found with id: " + categoryId, exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void getAllSupportRequests_success_shouldReturnAll_whenStatusIsNull() {
                SupportRequestSummaryResponse summary = createSummaryResponse(supportRequestId,
                                SupportRequestStatus.PENDING);

                when(supportRequestRepository.findAllByOrderByCreatedAtDesc())
                                .thenReturn(List.of(supportRequest));
                when(supportRequestMapper.toSummaryResponse(supportRequest))
                                .thenReturn(summary);

                List<SupportRequestSummaryResponse> response = supportRequestService.getAllSupportRequests(null);

                assertEquals(1, response.size());
                assertEquals(supportRequestId, response.get(0).getId());

                verify(supportRequestRepository).findAllByOrderByCreatedAtDesc();
                verify(supportRequestRepository, never()).findAllByStatusOrderByCreatedAtDesc(any());
        }

        @Test
        void getAllSupportRequests_success_shouldReturnByStatus_whenStatusProvided() {
                SupportRequestSummaryResponse summary = createSummaryResponse(supportRequestId,
                                SupportRequestStatus.PENDING);

                when(supportRequestRepository.findAllByStatusOrderByCreatedAtDesc(SupportRequestStatus.PENDING))
                                .thenReturn(List.of(supportRequest));
                when(supportRequestMapper.toSummaryResponse(supportRequest))
                                .thenReturn(summary);

                List<SupportRequestSummaryResponse> response = supportRequestService
                                .getAllSupportRequests(SupportRequestStatus.PENDING);

                assertEquals(1, response.size());
                assertEquals(SupportRequestStatus.PENDING, response.get(0).getStatus());

                verify(supportRequestRepository).findAllByStatusOrderByCreatedAtDesc(SupportRequestStatus.PENDING);
                verify(supportRequestRepository, never()).findAllByOrderByCreatedAtDesc();
        }

        @Test
        void getMySupportRequests_success_shouldReturnRequesterRequests() {
                SupportRequestSummaryResponse summary = createSummaryResponse(supportRequestId,
                                SupportRequestStatus.PENDING);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportRequestRepository.findAllByRequesterOrderByCreatedAtDesc(requester))
                                .thenReturn(List.of(supportRequest));
                when(supportRequestMapper.toSummaryResponse(supportRequest))
                                .thenReturn(summary);

                List<SupportRequestSummaryResponse> response = supportRequestService.getMySupportRequests(requesterId);

                assertEquals(1, response.size());
                assertEquals(supportRequestId, response.get(0).getId());

                verify(supportRequestRepository).findAllByRequesterOrderByCreatedAtDesc(requester);
        }

        @Test
        void getSupportRequestById_success_shouldReturnDetail() {
                SupportRequestDetailResponse expectedResponse = createDetailResponse(
                                supportRequestId,
                                SupportRequestStatus.PENDING);

                when(supportRequestRepository.findById(supportRequestId))
                                .thenReturn(Optional.of(supportRequest));
                when(supportRequestMapper.toDetailResponse(supportRequest))
                                .thenReturn(expectedResponse);

                SupportRequestDetailResponse response = supportRequestService.getSupportRequestById(supportRequestId);

                assertEquals(supportRequestId, response.getId());
                assertEquals(SupportRequestStatus.PENDING, response.getStatus());

                verify(supportRequestRepository).findById(supportRequestId);
                verify(supportRequestMapper).toDetailResponse(supportRequest);
        }

        @Test
        void getSupportRequestById_shouldThrowResourceNotFoundException_whenNotFound() {
                when(supportRequestRepository.findById(supportRequestId))
                                .thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> supportRequestService.getSupportRequestById(supportRequestId));

                assertEquals("Support request not found with id: " + supportRequestId, exception.getMessage());

                verify(supportRequestMapper, never()).toDetailResponse(any());
        }

        @Test
        void updateMySupportRequest_success_shouldUpdatePendingRequest() {
                UpdateSupportRequestRequest request = createUpdateRequest();

                SupportRequestDetailResponse expectedResponse = createDetailResponse(
                                supportRequestId,
                                SupportRequestStatus.PENDING);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
                when(supportRequestRepository.save(supportRequest)).thenReturn(supportRequest);
                when(supportRequestMapper.toDetailResponse(supportRequest)).thenReturn(expectedResponse);

                SupportRequestDetailResponse response = supportRequestService.updateMySupportRequest(requesterId,
                                supportRequestId, request);

                assertEquals(supportRequestId, response.getId());
                assertEquals("Updated title", supportRequest.getTitle());
                assertEquals("Updated description", supportRequest.getDescription());
                assertEquals("Updated address", supportRequest.getAddress());
                assertEquals(10.5, supportRequest.getLatitude());
                assertEquals(106.7, supportRequest.getLongitude());

                verify(supportRequestRepository).save(supportRequest);
        }

        @Test
        void updateMySupportRequest_shouldThrowForbiddenException_whenNotOwner() {
                UpdateSupportRequestRequest request = createUpdateRequest();
                User anotherRequester = createUser(UUID.randomUUID(), "Another Requester", UserRole.REQUESTER);

                when(userRepository.findById(anotherRequester.getId())).thenReturn(Optional.of(anotherRequester));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> supportRequestService.updateMySupportRequest(
                                                anotherRequester.getId(),
                                                supportRequestId,
                                                request));

                assertEquals("You can only update your own support request", exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void updateMySupportRequest_shouldThrowBadRequestException_whenRequestIsNotPending() {
                UpdateSupportRequestRequest request = createUpdateRequest();
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportRequestService.updateMySupportRequest(requesterId, supportRequestId,
                                                request));

                assertEquals("Only pending support request can be updated", exception.getMessage());

                verify(categoryRepository, never()).findById(any());
                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void approveSupportRequest_success_shouldApprovePendingRequest() {
                SupportRequestDetailResponse expectedResponse = createDetailResponse(
                                supportRequestId,
                                SupportRequestStatus.APPROVED);

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportRequestRepository.save(supportRequest)).thenReturn(supportRequest);
                when(supportRequestMapper.toDetailResponse(supportRequest)).thenReturn(expectedResponse);

                SupportRequestDetailResponse response = supportRequestService.approveSupportRequest(reviewerId,
                                supportRequestId);

                assertEquals(SupportRequestStatus.APPROVED, response.getStatus());
                assertEquals(SupportRequestStatus.APPROVED, supportRequest.getStatus());
                assertEquals(reviewer, supportRequest.getReviewedBy());
                assertNotNull(supportRequest.getReviewedAt());
                assertNull(supportRequest.getRejectionReason());

                verify(supportRequestRepository).save(supportRequest);
        }

        @Test
        void approveSupportRequest_shouldThrowForbiddenException_whenReviewerInvalid() {
                when(userRepository.findById(volunteer.getId())).thenReturn(Optional.of(volunteer));

                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> supportRequestService.approveSupportRequest(volunteer.getId(), supportRequestId));

                assertEquals("Only admin or collaborator can review support request", exception.getMessage());

                verify(supportRequestRepository, never()).findById(any());
                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void approveSupportRequest_shouldThrowBadRequestException_whenRequestIsNotPending() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportRequestService.approveSupportRequest(reviewerId, supportRequestId));

                assertEquals("Only pending support request can be approved", exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void rejectSupportRequest_success_shouldRejectPendingRequest() {
                RejectSupportRequestRequest request = new RejectSupportRequestRequest();
                request.setRejectionReason(" Missing evidence ");

                SupportRequestDetailResponse expectedResponse = createDetailResponse(
                                supportRequestId,
                                SupportRequestStatus.REJECTED);
                expectedResponse.setRejectionReason("Missing evidence");

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportRequestRepository.save(supportRequest)).thenReturn(supportRequest);
                when(supportRequestMapper.toDetailResponse(supportRequest)).thenReturn(expectedResponse);

                SupportRequestDetailResponse response = supportRequestService.rejectSupportRequest(reviewerId,
                                supportRequestId,
                                request);

                assertEquals(SupportRequestStatus.REJECTED, response.getStatus());
                assertEquals("Missing evidence", supportRequest.getRejectionReason());
                assertEquals(reviewer, supportRequest.getReviewedBy());
                assertNotNull(supportRequest.getReviewedAt());

                verify(supportRequestRepository).save(supportRequest);
        }

        @Test
        void rejectSupportRequest_shouldThrowBadRequestException_whenReasonBlank() {
                RejectSupportRequestRequest request = new RejectSupportRequestRequest();
                request.setRejectionReason("   ");

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportRequestService.rejectSupportRequest(reviewerId, supportRequestId,
                                                request));

                assertEquals("Rejection reason is required", exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void rejectSupportRequest_shouldThrowBadRequestException_whenRequestIsNotPending() {
                RejectSupportRequestRequest request = new RejectSupportRequestRequest();
                request.setRejectionReason("Invalid request");

                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportRequestService.rejectSupportRequest(reviewerId, supportRequestId,
                                                request));

                assertEquals("Only pending support request can be rejected", exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void assignSupportRequestToSupportLocation_success_shouldAssignLocation() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                AssignSupportRequestToSupportLocationRequest request = new AssignSupportRequestToSupportLocationRequest();
                request.setSupportLocationId(supportLocationId);

                SupportRequestDetailResponse expectedResponse = createDetailResponse(
                                supportRequestId,
                                SupportRequestStatus.APPROVED);
                expectedResponse.setAssignedSupportLocationId(supportLocationId);
                expectedResponse.setAssignedSupportLocationName("Main Support Location");

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportLocationRepository.findById(supportLocationId)).thenReturn(Optional.of(supportLocation));
                when(supportRequestRepository.save(supportRequest)).thenReturn(supportRequest);
                when(supportRequestMapper.toDetailResponse(supportRequest)).thenReturn(expectedResponse);

                SupportRequestDetailResponse response = supportRequestService.assignSupportRequestToSupportLocation(
                                reviewerId,
                                supportRequestId,
                                request);

                assertEquals(supportLocationId, response.getAssignedSupportLocationId());
                assertEquals(supportLocation, supportRequest.getAssignedSupportLocation());

                verify(supportRequestRepository).save(supportRequest);
        }

        @Test
        void assignSupportRequestToSupportLocation_shouldThrowBadRequestException_whenRequestNotApproved() {
                AssignSupportRequestToSupportLocationRequest request = new AssignSupportRequestToSupportLocationRequest();
                request.setSupportLocationId(supportLocationId);

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportRequestService.assignSupportRequestToSupportLocation(
                                                reviewerId,
                                                supportRequestId,
                                                request));

                assertEquals("Only approved support request can be assigned to support location",
                                exception.getMessage());

                verify(supportLocationRepository, never()).findById(any());
                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void assignSupportRequestToSupportLocation_shouldThrowResourceNotFoundException_whenLocationNotFound() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                AssignSupportRequestToSupportLocationRequest request = new AssignSupportRequestToSupportLocationRequest();
                request.setSupportLocationId(supportLocationId);

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportLocationRepository.findById(supportLocationId)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> supportRequestService.assignSupportRequestToSupportLocation(
                                                reviewerId,
                                                supportRequestId,
                                                request));

                assertEquals("Support location not found with id: " + supportLocationId, exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        @Test
        void assignSupportRequestToSupportLocation_shouldThrowBadRequestException_whenLocationInactive() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);
                supportLocation.setIsActive(false);

                AssignSupportRequestToSupportLocationRequest request = new AssignSupportRequestToSupportLocationRequest();
                request.setSupportLocationId(supportLocationId);

                when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportLocationRepository.findById(supportLocationId)).thenReturn(Optional.of(supportLocation));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportRequestService.assignSupportRequestToSupportLocation(
                                                reviewerId,
                                                supportRequestId,
                                                request));

                assertEquals("Support location is inactive", exception.getMessage());

                verify(supportRequestRepository, never()).save(any());
        }

        private CreateSupportRequestRequest createCreateRequest() {
                CreateSupportRequestRequest request = new CreateSupportRequestRequest();
                request.setTitle(" Need medical support ");
                request.setDescription(" Need medicine ");
                request.setCategoryId(categoryId);
                request.setAddress(" Ho Chi Minh City ");
                request.setLatitude(10.1);
                request.setLongitude(106.6);
                return request;
        }

        private UpdateSupportRequestRequest createUpdateRequest() {
                UpdateSupportRequestRequest request = new UpdateSupportRequestRequest();
                request.setTitle(" Updated title ");
                request.setDescription(" Updated description ");
                request.setCategoryId(categoryId);
                request.setAddress(" Updated address ");
                request.setLatitude(10.5);
                request.setLongitude(106.7);
                return request;
        }

        private User createUser(UUID id, String fullName, UserRole role) {
                User user = new User();
                user.setId(id);
                user.setFullName(fullName);
                user.setEmail(fullName.toLowerCase().replace(" ", ".") + "@example.com");
                user.setRole(role);
                user.setIsActive(true);
                user.setAvatarUrl("https://example.com/avatar.png");
                return user;
        }

        private Category createCategory(UUID id, String name, String code, Boolean isActive) {
                Category category = new Category();
                category.setId(id);
                category.setName(name);
                category.setCode(code);
                category.setIsActive(isActive);
                return category;
        }

        private SupportLocation createSupportLocation(UUID id, String name, Boolean isActive) {
                SupportLocation supportLocation = new SupportLocation();
                supportLocation.setId(id);
                supportLocation.setName(name);
                supportLocation.setIsActive(isActive);
                return supportLocation;
        }

        private SupportRequest createSupportRequest(
                        UUID id,
                        String title,
                        String description,
                        SupportRequestStatus status,
                        User requester,
                        Category category) {

                SupportRequest supportRequest = new SupportRequest();
                supportRequest.setId(id);
                supportRequest.setTitle(title);
                supportRequest.setDescription(description);
                supportRequest.setStatus(status);
                supportRequest.setRequester(requester);
                supportRequest.setCategory(category);
                supportRequest.setAddress("Ho Chi Minh City");
                supportRequest.setLatitude(10.1);
                supportRequest.setLongitude(106.6);
                return supportRequest;
        }

        private SupportRequestDetailResponse createDetailResponse(UUID id, SupportRequestStatus status) {
                return SupportRequestDetailResponse.builder()
                                .id(id)
                                .title("Need medical support")
                                .description("Need medicine")
                                .categoryId(categoryId)
                                .categoryName("Medical")
                                .requesterId(requesterId)
                                .requesterName("Requester")
                                .status(status)
                                .address("Ho Chi Minh City")
                                .latitude(10.1)
                                .longitude(106.6)
                                .build();
        }

        private SupportRequestSummaryResponse createSummaryResponse(UUID id, SupportRequestStatus status) {
                return SupportRequestSummaryResponse.builder()
                                .id(id)
                                .title("Need medical support")
                                .categoryId(categoryId)
                                .categoryName("Medical")
                                .requesterId(requesterId)
                                .requesterName("Requester")
                                .status(status)
                                .address("Ho Chi Minh City")
                                .latitude(10.1)
                                .longitude(106.6)
                                .build();
        }
}