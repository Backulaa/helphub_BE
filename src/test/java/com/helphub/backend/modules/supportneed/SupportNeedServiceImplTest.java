package com.helphub.backend.modules.supportneed;

import com.helphub.backend.common.enums.SupportNeedUnit;
import com.helphub.backend.common.enums.SupportNeedContributionStatus;
import com.helphub.backend.common.enums.SupportRequestStatus;
import com.helphub.backend.common.enums.SupportType;
import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.enums.VolunteerAssignmentStatus;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ForbiddenException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.supportneed.dto.request.CreateSupportNeedContributionRequest;
import com.helphub.backend.modules.supportneed.dto.request.CreateSupportNeedRequest;
import com.helphub.backend.modules.supportneed.dto.request.UpdateSupportNeedRequest;
import com.helphub.backend.modules.supportneed.dto.response.SupportNeedContributionResponse;
import com.helphub.backend.modules.supportneed.dto.response.SupportNeedResponse;
import com.helphub.backend.persistence.entity.Category;
import com.helphub.backend.persistence.entity.SupportNeed;
import com.helphub.backend.persistence.entity.SupportNeedContribution;
import com.helphub.backend.persistence.entity.SupportRequest;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.SupportNeedContributionRepository;
import com.helphub.backend.persistence.repository.SupportNeedRepository;
import com.helphub.backend.persistence.repository.SupportRequestRepository;
import com.helphub.backend.persistence.repository.UserRepository;
import com.helphub.backend.persistence.repository.VolunteerAssignmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportNeedServiceImplTest {

        @Mock
        private SupportNeedRepository supportNeedRepository;

        @Mock
        private SupportNeedContributionRepository supportNeedContributionRepository;

        @Mock
        private SupportRequestRepository supportRequestRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private VolunteerAssignmentRepository volunteerAssignmentRepository;

        @Mock
        private SupportNeedMapper supportNeedMapper;

        @InjectMocks
        private SupportNeedServiceImpl supportNeedService;

        private UUID requesterId;
        private UUID supportRequestId;
        private UUID supportNeedId;
        private UUID contributorId;

        private User requester;
        private User collaborator;
        private User volunteer;
        private Category category;
        private SupportRequest supportRequest;
        private SupportNeed supportNeed;

        @BeforeEach
        void setUp() {
                requesterId = UUID.randomUUID();
                supportRequestId = UUID.randomUUID();
                supportNeedId = UUID.randomUUID();
                contributorId = UUID.randomUUID();

                requester = createUser(requesterId, "Requester", UserRole.REQUESTER);
                collaborator = createUser(contributorId, "Collaborator", UserRole.COLLABORATOR);
                volunteer = createUser(UUID.randomUUID(), "Volunteer", UserRole.VOLUNTEER);

                category = new Category();
                category.setId(UUID.randomUUID());
                category.setName("Food");
                category.setCode("FOOD");
                category.setIsActive(true);

                supportRequest = createSupportRequest(SupportRequestStatus.PENDING);
                supportNeed = createSupportNeed(BigDecimal.valueOf(10), BigDecimal.ZERO);
        }

        @Test
        void createSupportNeed_success_shouldCreateSupportNeed() {
                CreateSupportNeedRequest request = createCreateRequest();

                SupportNeedResponse expectedResponse = createNeedResponse(BigDecimal.valueOf(10), BigDecimal.ZERO);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportNeedRepository.existsBySupportRequestAndNeedNameIgnoreCase(supportRequest, "Rice"))
                                .thenReturn(false);
                when(supportNeedRepository.save(any(SupportNeed.class)))
                                .thenAnswer(invocation -> {
                                        SupportNeed saved = invocation.getArgument(0);
                                        saved.setId(supportNeedId);
                                        return saved;
                                });
                when(supportNeedMapper.toResponse(any(SupportNeed.class))).thenReturn(expectedResponse);

                SupportNeedResponse response = supportNeedService.createSupportNeed(
                                requesterId,
                                supportRequestId,
                                request);

                assertNotNull(response);
                assertEquals(supportNeedId, response.getId());
                assertEquals("Rice", response.getNeedName());
                assertEquals(BigDecimal.valueOf(10), response.getRequiredQuantity());

                verify(supportNeedRepository).save(any(SupportNeed.class));
        }

        @Test
        void createSupportNeed_shouldThrowForbiddenException_whenUserIsNotRequester() {
                CreateSupportNeedRequest request = createCreateRequest();

                User volunteerUser = createUser(UUID.randomUUID(), "Volunteer", UserRole.VOLUNTEER);

                when(userRepository.findById(volunteerUser.getId())).thenReturn(Optional.of(volunteerUser));

                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> supportNeedService.createSupportNeed(
                                                volunteerUser.getId(),
                                                supportRequestId,
                                                request));

                assertEquals("Only requester can manage support needs", exception.getMessage());

                verify(supportRequestRepository, never()).findById(any());
                verify(supportNeedRepository, never()).save(any());
        }

        @Test
        void createSupportNeed_shouldThrowForbiddenException_whenNotOwner() {
                CreateSupportNeedRequest request = createCreateRequest();

                User anotherRequester = createUser(UUID.randomUUID(), "Another", UserRole.REQUESTER);

                when(userRepository.findById(anotherRequester.getId())).thenReturn(Optional.of(anotherRequester));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> supportNeedService.createSupportNeed(
                                                anotherRequester.getId(),
                                                supportRequestId,
                                                request));

                assertEquals("You can only manage needs of your own support request", exception.getMessage());

                verify(supportNeedRepository, never()).save(any());
        }

        @Test
        void createSupportNeed_shouldThrowBadRequestException_whenSupportRequestStatusInvalid() {
                CreateSupportNeedRequest request = createCreateRequest();
                supportRequest.setStatus(SupportRequestStatus.IN_PROGRESS);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportNeedService.createSupportNeed(
                                                requesterId,
                                                supportRequestId,
                                                request));

                assertEquals("Support needs can only be modified when support request is pending or approved",
                                exception.getMessage());

                verify(supportNeedRepository, never()).save(any());
        }

        @Test
        void createSupportNeed_shouldThrowBadRequestException_whenDuplicateNeedName() {
                CreateSupportNeedRequest request = createCreateRequest();

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportNeedRepository.existsBySupportRequestAndNeedNameIgnoreCase(supportRequest, "Rice"))
                                .thenReturn(true);

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportNeedService.createSupportNeed(
                                                requesterId,
                                                supportRequestId,
                                                request));

                assertEquals("Support need already exists in this support request", exception.getMessage());

                verify(supportNeedRepository, never()).save(any());
        }

        @Test
        void getSupportNeedsBySupportRequest_success_shouldReturnNeeds() {
                SupportNeedResponse expectedResponse = createNeedResponse(BigDecimal.valueOf(10), BigDecimal.ZERO);

                when(supportRequestRepository.findById(supportRequestId)).thenReturn(Optional.of(supportRequest));
                when(supportNeedRepository.findAllBySupportRequestOrderByCreatedAtDesc(supportRequest))
                                .thenReturn(List.of(supportNeed));
                when(supportNeedMapper.toResponse(supportNeed)).thenReturn(expectedResponse);

                List<SupportNeedResponse> response = supportNeedService
                                .getSupportNeedsBySupportRequest(supportRequestId);

                assertEquals(1, response.size());
                assertEquals(supportNeedId, response.get(0).getId());

                verify(supportNeedRepository).findAllBySupportRequestOrderByCreatedAtDesc(supportRequest);
        }

        @Test
        void updateSupportNeed_success_shouldUpdateNeed() {
                UpdateSupportNeedRequest request = createUpdateRequest();

                SupportNeedResponse expectedResponse = createNeedResponse(BigDecimal.valueOf(20), BigDecimal.ZERO);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));
                when(supportNeedRepository.save(supportNeed)).thenReturn(supportNeed);
                when(supportNeedMapper.toResponse(supportNeed)).thenReturn(expectedResponse);

                SupportNeedResponse response = supportNeedService.updateSupportNeed(
                                requesterId,
                                supportNeedId,
                                request);

                assertEquals(BigDecimal.valueOf(20), response.getRequiredQuantity());
                assertEquals("Rice bags", supportNeed.getNeedName());
                assertEquals(SupportType.GOODS, supportNeed.getSupportType());
                assertEquals(SupportNeedUnit.BOX, supportNeed.getUnit());

                verify(supportNeedRepository).save(supportNeed);
        }

        @Test
        void updateSupportNeed_shouldThrowBadRequestException_whenRequiredQuantityLessThanReceived() {
                supportNeed.setReceivedQuantity(BigDecimal.valueOf(8));

                UpdateSupportNeedRequest request = createUpdateRequest();
                request.setRequiredQuantity(BigDecimal.valueOf(5));

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportNeedService.updateSupportNeed(
                                                requesterId,
                                                supportNeedId,
                                                request));

                assertEquals("Required quantity cannot be less than received quantity", exception.getMessage());

                verify(supportNeedRepository, never()).save(any());
        }

        @Test
        void deleteSupportNeed_success_shouldDeleteNeed() {
                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));

                supportNeedService.deleteSupportNeed(requesterId, supportNeedId);

                verify(supportNeedRepository).delete(supportNeed);
        }

        @Test
        void deleteSupportNeed_shouldThrowBadRequestException_whenNeedHasContribution() {
                supportNeed.setReceivedQuantity(BigDecimal.ONE);

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportNeedService.deleteSupportNeed(requesterId, supportNeedId));

                assertEquals("Cannot delete support need that already has contributions", exception.getMessage());

                verify(supportNeedRepository, never()).delete(any());
        }

        @Test
        void contributeToSupportNeed_success_shouldAllowCollaboratorAndUpdateToInProgress() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                CreateSupportNeedContributionRequest request = createContributionRequest(BigDecimal.valueOf(5));

                SupportNeedContribution contribution = createContribution(collaborator, BigDecimal.valueOf(5));

                SupportNeedContributionResponse expectedResponse = createContributionResponse(contribution.getId());

                when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));
                when(supportNeedContributionRepository.save(any(SupportNeedContribution.class)))
                                .thenReturn(contribution);
                when(supportNeedRepository.save(supportNeed)).thenReturn(supportNeed);
                when(supportNeedRepository.findAllBySupportRequestOrderByCreatedAtDesc(supportRequest))
                                .thenReturn(List.of(supportNeed));
                when(supportRequestRepository.save(supportRequest)).thenReturn(supportRequest);
                when(supportNeedMapper.toContributionResponse(contribution)).thenReturn(expectedResponse);

                SupportNeedContributionResponse response = supportNeedService.contributeToSupportNeed(
                                collaborator.getId(),
                                supportNeedId,
                                request);

                assertNotNull(response);
                assertEquals(BigDecimal.valueOf(5), supportNeed.getReceivedQuantity());
                assertEquals(SupportRequestStatus.IN_PROGRESS, supportRequest.getStatus());

                verify(supportNeedContributionRepository).save(any(SupportNeedContribution.class));
                verify(supportNeedRepository).save(supportNeed);
                verify(supportRequestRepository).save(supportRequest);
        }

        @Test
        void contributeToSupportNeed_success_shouldCompleteSupportRequest_whenAllNeedsFulfilled() {
                supportRequest.setStatus(SupportRequestStatus.IN_PROGRESS);

                CreateSupportNeedContributionRequest request = createContributionRequest(BigDecimal.valueOf(10));

                SupportNeedContribution contribution = createContribution(collaborator, BigDecimal.valueOf(10));
                SupportNeedContributionResponse expectedResponse = createContributionResponse(contribution.getId());

                when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));
                when(supportNeedContributionRepository.save(any(SupportNeedContribution.class)))
                                .thenReturn(contribution);
                when(supportNeedRepository.save(supportNeed)).thenReturn(supportNeed);
                when(supportNeedRepository.findAllBySupportRequestOrderByCreatedAtDesc(supportRequest))
                                .thenReturn(List.of(supportNeed));
                when(supportRequestRepository.save(supportRequest)).thenReturn(supportRequest);
                when(supportNeedMapper.toContributionResponse(contribution)).thenReturn(expectedResponse);

                SupportNeedContributionResponse response = supportNeedService.contributeToSupportNeed(
                                collaborator.getId(),
                                supportNeedId,
                                request);

                assertNotNull(response);
                assertEquals(BigDecimal.valueOf(10), supportNeed.getReceivedQuantity());
                assertEquals(SupportRequestStatus.COMPLETED, supportRequest.getStatus());

                verify(supportRequestRepository).save(supportRequest);
        }

        @Test
        void contributeToSupportNeed_success_shouldAllowAcceptedVolunteer() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                CreateSupportNeedContributionRequest request = createContributionRequest(BigDecimal.valueOf(3));

                SupportNeedContribution contribution = createContribution(volunteer, BigDecimal.valueOf(3));
                SupportNeedContributionResponse expectedResponse = createContributionResponse(contribution.getId());

                when(userRepository.findById(volunteer.getId())).thenReturn(Optional.of(volunteer));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));
                when(volunteerAssignmentRepository.existsBySupportRequestAndVolunteerAndStatus(
                                supportRequest,
                                volunteer,
                                VolunteerAssignmentStatus.ACCEPTED))
                                .thenReturn(true);
                when(supportNeedContributionRepository.save(any(SupportNeedContribution.class)))
                                .thenReturn(contribution);
                when(supportNeedRepository.save(supportNeed)).thenReturn(supportNeed);
                when(supportNeedRepository.findAllBySupportRequestOrderByCreatedAtDesc(supportRequest))
                                .thenReturn(List.of(supportNeed));
                when(supportRequestRepository.save(supportRequest)).thenReturn(supportRequest);
                when(supportNeedMapper.toContributionResponse(contribution)).thenReturn(expectedResponse);

                SupportNeedContributionResponse response = supportNeedService.contributeToSupportNeed(
                                volunteer.getId(),
                                supportNeedId,
                                request);

                assertNotNull(response);
                assertEquals(BigDecimal.valueOf(3), supportNeed.getReceivedQuantity());

                verify(volunteerAssignmentRepository).existsBySupportRequestAndVolunteerAndStatus(
                                supportRequest,
                                volunteer,
                                VolunteerAssignmentStatus.ACCEPTED);
        }

        @Test
        void contributeToSupportNeed_shouldThrowForbiddenException_whenVolunteerNotAccepted() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                CreateSupportNeedContributionRequest request = createContributionRequest(BigDecimal.valueOf(3));

                when(userRepository.findById(volunteer.getId())).thenReturn(Optional.of(volunteer));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));
                when(volunteerAssignmentRepository.existsBySupportRequestAndVolunteerAndStatus(
                                supportRequest,
                                volunteer,
                                VolunteerAssignmentStatus.ACCEPTED))
                                .thenReturn(false);

                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> supportNeedService.contributeToSupportNeed(
                                                volunteer.getId(),
                                                supportNeedId,
                                                request));

                assertEquals("Volunteer must be accepted for this support request before contributing",
                                exception.getMessage());

                verify(supportNeedContributionRepository, never()).save(any());
        }

        @Test
        void contributeToSupportNeed_shouldThrowForbiddenException_whenRequesterContributesOwnNeed() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);

                CreateSupportNeedContributionRequest request = createContributionRequest(BigDecimal.valueOf(3));

                when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));

                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> supportNeedService.contributeToSupportNeed(
                                                requesterId,
                                                supportNeedId,
                                                request));

                assertEquals("Only assigned volunteer or collaborator can contribute to support need",
                                exception.getMessage());

                verify(supportNeedContributionRepository, never()).save(any());
        }

        @Test
        void contributeToSupportNeed_shouldThrowBadRequestException_whenSupportRequestNotApprovedOrInProgress() {
                supportRequest.setStatus(SupportRequestStatus.PENDING);

                CreateSupportNeedContributionRequest request = createContributionRequest(BigDecimal.valueOf(3));

                when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportNeedService.contributeToSupportNeed(
                                                collaborator.getId(),
                                                supportNeedId,
                                                request));

                assertEquals("Support request must be approved or in progress to receive contributions",
                                exception.getMessage());

                verify(supportNeedContributionRepository, never()).save(any());
        }

        @Test
        void contributeToSupportNeed_shouldThrowBadRequestException_whenQuantityExceedsRemaining() {
                supportRequest.setStatus(SupportRequestStatus.APPROVED);
                supportNeed.setReceivedQuantity(BigDecimal.valueOf(8));

                CreateSupportNeedContributionRequest request = createContributionRequest(BigDecimal.valueOf(5));

                when(userRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> supportNeedService.contributeToSupportNeed(
                                                collaborator.getId(),
                                                supportNeedId,
                                                request));

                assertTrue(exception.getMessage().contains("Contribution quantity exceeds remaining quantity"));

                verify(supportNeedContributionRepository, never()).save(any());
        }

        @Test
        void getContributionsBySupportNeed_success_shouldReturnContributions() {
                SupportNeedContribution contribution = createContribution(collaborator, BigDecimal.valueOf(5));
                SupportNeedContributionResponse expectedResponse = createContributionResponse(contribution.getId());

                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.of(supportNeed));
                when(supportNeedContributionRepository.findAllBySupportNeedAndStatusOrderByCreatedAtDesc(
                                supportNeed,
                                SupportNeedContributionStatus.SUCCESS))
                                .thenReturn(List.of(contribution));
                when(supportNeedMapper.toContributionResponse(contribution)).thenReturn(expectedResponse);

                List<SupportNeedContributionResponse> response = supportNeedService
                                .getContributionsBySupportNeed(supportNeedId);

                assertEquals(1, response.size());
                assertEquals(contribution.getId(), response.get(0).getId());

                verify(supportNeedContributionRepository).findAllBySupportNeedAndStatusOrderByCreatedAtDesc(
                                supportNeed,
                                SupportNeedContributionStatus.SUCCESS);
        }

        @Test
        void getContributionsBySupportNeed_shouldThrowResourceNotFoundException_whenNeedNotFound() {
                when(supportNeedRepository.findById(supportNeedId)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> supportNeedService.getContributionsBySupportNeed(supportNeedId));

                assertEquals("Support need not found with id: " + supportNeedId, exception.getMessage());
        }

        private CreateSupportNeedRequest createCreateRequest() {
                CreateSupportNeedRequest request = new CreateSupportNeedRequest();
                request.setSupportType(SupportType.GOODS);
                request.setNeedName(" Rice ");
                request.setUnit(SupportNeedUnit.KG);
                request.setRequiredQuantity(BigDecimal.valueOf(10));
                return request;
        }

        private UpdateSupportNeedRequest createUpdateRequest() {
                UpdateSupportNeedRequest request = new UpdateSupportNeedRequest();
                request.setSupportType(SupportType.GOODS);
                request.setNeedName(" Rice bags ");
                request.setUnit(SupportNeedUnit.BOX);
                request.setRequiredQuantity(BigDecimal.valueOf(20));
                return request;
        }

        private CreateSupportNeedContributionRequest createContributionRequest(BigDecimal quantity) {
                CreateSupportNeedContributionRequest request = new CreateSupportNeedContributionRequest();
                request.setQuantity(quantity);
                request.setNote(" Support note ");
                return request;
        }

        private User createUser(UUID id, String fullName, UserRole role) {
                User user = new User();
                user.setId(id);
                user.setFullName(fullName);
                user.setEmail(fullName.toLowerCase().replace(" ", ".") + "@example.com");
                user.setRole(role);
                user.setIsActive(true);
                return user;
        }

        private SupportRequest createSupportRequest(SupportRequestStatus status) {
                SupportRequest supportRequest = new SupportRequest();
                supportRequest.setId(supportRequestId);
                supportRequest.setTitle("Need food");
                supportRequest.setDescription("Need rice");
                supportRequest.setRequester(requester);
                supportRequest.setCategory(category);
                supportRequest.setStatus(status);
                return supportRequest;
        }

        private SupportNeed createSupportNeed(BigDecimal requiredQuantity, BigDecimal receivedQuantity) {
                SupportNeed need = new SupportNeed();
                need.setId(supportNeedId);
                need.setSupportRequest(supportRequest);
                need.setSupportType(SupportType.GOODS);
                need.setNeedName("Rice");
                need.setUnit(SupportNeedUnit.KG);
                need.setRequiredQuantity(requiredQuantity);
                need.setReceivedQuantity(receivedQuantity);
                return need;
        }

        private SupportNeedContribution createContribution(User contributor, BigDecimal quantity) {
                SupportNeedContribution contribution = new SupportNeedContribution();
                contribution.setId(UUID.randomUUID());
                contribution.setSupportNeed(supportNeed);
                contribution.setContributor(contributor);
                contribution.setQuantity(quantity);
                contribution.setNote("Support note");
                return contribution;
        }

        private SupportNeedResponse createNeedResponse(BigDecimal requiredQuantity, BigDecimal receivedQuantity) {
                return SupportNeedResponse.builder()
                                .id(supportNeedId)
                                .supportRequestId(supportRequestId)
                                .supportRequestTitle("Need food")
                                .supportType(SupportType.GOODS)
                                .needName("Rice")
                                .unit(SupportNeedUnit.KG)
                                .requiredQuantity(requiredQuantity)
                                .receivedQuantity(receivedQuantity)
                                .remainingQuantity(requiredQuantity.subtract(receivedQuantity))
                                .isFulfilled(receivedQuantity.compareTo(requiredQuantity) >= 0)
                                .build();
        }

        private SupportNeedContributionResponse createContributionResponse(UUID contributionId) {
                return SupportNeedContributionResponse.builder()
                                .id(contributionId)
                                .supportNeedId(supportNeedId)
                                .needName("Rice")
                                .contributorId(collaborator.getId())
                                .contributorName("Collaborator")
                                .quantity(BigDecimal.valueOf(5))
                                .note("Support note")
                                .build();
        }
}