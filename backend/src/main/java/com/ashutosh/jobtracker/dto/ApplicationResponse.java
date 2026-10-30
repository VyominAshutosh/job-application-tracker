package com.ashutosh.jobtracker.dto;

import com.ashutosh.jobtracker.entity.ApplicationStatus;
import com.ashutosh.jobtracker.entity.JobApplication;
import java.time.LocalDate;
import java.util.Set;

/** What the API sends back for one application. */
public record ApplicationResponse(
        Long id,
        String company,
        String role,
        String location,
        String source,
        LocalDate appliedOn,
        LocalDate interviewDate,
        ApplicationStatus status,
        String notes,
        Set<ApplicationStatus> allowedNext
) {
    public static ApplicationResponse from(JobApplication a) {
        return new ApplicationResponse(
                a.getId(), a.getCompany(), a.getRole(), a.getLocation(), a.getSource(),
                a.getAppliedOn(), a.getInterviewDate(), a.getStatus(), a.getNotes(),
                a.getStatus().allowedNext());
    }
}
