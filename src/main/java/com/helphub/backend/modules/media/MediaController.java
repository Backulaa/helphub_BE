package com.helphub.backend.modules.media;

import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.media.dto.request.CreateMediaRequest;
import com.helphub.backend.modules.media.dto.request.UpdateMediaRequest;
import com.helphub.backend.modules.media.dto.response.MediaDetailResponse;
import com.helphub.backend.modules.media.dto.response.MediaSummaryResponse;
import com.helphub.backend.security.model.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
