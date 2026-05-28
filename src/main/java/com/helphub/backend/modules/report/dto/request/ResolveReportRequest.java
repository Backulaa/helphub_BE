package com.helphub.backend.modules.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolveReportRequest {

    @NotBlank(message = "Resolution note is required")
    @Size(max = 1000, message = "Resolution note must not exceed 1000 characters")
    private String resolutionNote;

    @Size(max = 200, message = "Rejection reason must not exceed 200 characters")
    private String supportRequestRejectionReason;
}