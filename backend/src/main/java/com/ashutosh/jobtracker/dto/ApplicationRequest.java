package com.ashutosh.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * What the client is allowed to send in. Note there is no status field:
 * every application enters the pipeline at APPLIED, and moves only through
 * the status endpoint. A record is a compact class whose fields are final.
 */
public record ApplicationRequest(

        @NotBlank(message = "company is required")
        @Size(max = 120, message = "company must be at most 120 characters")
        String company,

        @NotBlank(message = "role is required")
        @Size(max = 120, message = "role must be at most 120 characters")
        String role,

        @Size(max = 120, message = "location must be at most 120 characters")
        String location,

        @Size(max = 60, message = "source must be at most 60 characters")
        String source,

        @NotNull(message = "appliedOn is required, format yyyy-MM-dd")
        LocalDate appliedOn,

        /** Optional. Format yyyy-MM-dd. */
        LocalDate interviewDate,

        @Size(max = 500, message = "notes must be at most 500 characters")
        String notes
) {
}
