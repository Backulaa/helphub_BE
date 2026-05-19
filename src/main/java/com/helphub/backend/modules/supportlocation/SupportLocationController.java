package com.helphub.backend.modules.supportlocation;

import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.supportlocation.dto.request.CreateSupportLocationRequest;
import com.helphub.backend.modules.supportlocation.dto.request.UpdateSupportLocationRequest;
import com.helphub.backend.modules.supportlocation.dto.request.UpdateSupportLocationStatusRequest;
import com.helphub.backend.modules.supportlocation.dto.response.SupportLocationDetailResponse;
import com.helphub.backend.modules.supportlocation.dto.response.SupportLocationSummaryResponse;
import com.helphub.backend.security.model.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
