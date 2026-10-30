package com.ashutosh.jobtracker.dto;

import com.ashutosh.jobtracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(

        @NotNull(message = "status is required")
        ApplicationStatus status
) {
}
