package com.helphub.backend.modules.message;

import com.helphub.backend.common.payload.ApiResponse;
import com.helphub.backend.modules.message.dto.request.SendMessageRequest;
import com.helphub.backend.modules.message.dto.request.UpdateMessageRequest;
import com.helphub.backend.modules.message.dto.response.MessageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
