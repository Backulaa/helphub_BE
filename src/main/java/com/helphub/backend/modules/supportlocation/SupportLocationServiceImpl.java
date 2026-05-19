package com.helphub.backend.modules.supportlocation;

import com.helphub.backend.common.enums.UserRole;
import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ForbiddenException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.supportlocation.dto.request.CreateSupportLocationRequest;
import com.helphub.backend.modules.supportlocation.dto.request.UpdateSupportLocationRequest;
import com.helphub.backend.modules.supportlocation.dto.request.UpdateSupportLocationStatusRequest;
import com.helphub.backend.modules.supportlocation.dto.response.SupportLocationDetailResponse;
import com.helphub.backend.modules.supportlocation.dto.response.SupportLocationSummaryResponse;
import com.helphub.backend.persistence.entity.SupportLocation;
import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.SupportLocationRepository;
import com.helphub.backend.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
