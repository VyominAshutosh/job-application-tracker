package com.ashutosh.jobtracker.service;

import com.ashutosh.jobtracker.dto.ApplicationRequest;
import com.ashutosh.jobtracker.dto.ApplicationResponse;
import com.ashutosh.jobtracker.entity.ApplicationStatus;
import com.ashutosh.jobtracker.entity.JobApplication;
import com.ashutosh.jobtracker.exception.InvalidStatusTransitionException;
import com.ashutosh.jobtracker.exception.ResourceNotFoundException;
import com.ashutosh.jobtracker.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All the rules live here. The controller only moves data in and out,
 * and the repository only talks to the database.
 */
@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    /** Constructor injection. One dependency, final, impossible to forget. */
    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public ApplicationResponse create(ApplicationRequest request) {
        JobApplication application = new JobApplication(
                request.company(), request.role(), request.location(), request.source(),
                request.appliedOn(), request.interviewDate(), request.notes());
        JobApplication saved = repository.save(application);
        return ApplicationResponse.from(saved);
    }

    public List<ApplicationResponse> findAll(ApplicationStatus status) {
        List<JobApplication> rows = (status == null)
                ? repository.findAllByOrderByAppliedOnDesc()
                : repository.findByStatusOrderByAppliedOnDesc(status);
        return toResponses(rows);
    }

    public List<ApplicationResponse> search(String text) {
        if (text == null || text.isBlank()) {
            return findAll(null);
        }
        return toResponses(repository.search(text.trim()));
    }

    public ApplicationResponse findById(Long id) {
        return ApplicationResponse.from(getOrThrow(id));
    }

    public ApplicationResponse update(Long id, ApplicationRequest request) {
        JobApplication application = getOrThrow(id);
        application.setCompany(request.company());
        application.setRole(request.role());
        application.setLocation(request.location());
        application.setSource(request.source());
        application.setAppliedOn(request.appliedOn());
        application.setInterviewDate(request.interviewDate());
        application.setNotes(request.notes());
        return ApplicationResponse.from(repository.save(application));
    }

    /**
     * The one piece of real business logic. A status may only move forward one stage,
     * or sideways to REJECTED. OFFER and REJECTED are final.
     */
    public ApplicationResponse changeStatus(Long id, ApplicationStatus target) {
        JobApplication application = getOrThrow(id);
        ApplicationStatus current = application.getStatus();

        if (current == target) {
            throw new InvalidStatusTransitionException(
                    "Application " + id + " is already " + current + ".");
        }
        if (!current.canMoveTo(target)) {
            throw new InvalidStatusTransitionException(
                    "Cannot move application " + id + " from " + current + " to " + target
                            + ". Allowed from " + current + ": " + current.allowedNext() + ".");
        }

        application.setStatus(target);
        return ApplicationResponse.from(repository.save(application));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("No application with id " + id + ".");
        }
        repository.deleteById(id);
    }

    /** Counts per stage, for the summary row on the dashboard. */
    public Map<String, Long> stats() {
        Map<String, Long> counts = new LinkedHashMap<>();
        long total = 0;
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = repository.countByStatus(status);
            counts.put(status.name(), count);
            total += count;
        }
        counts.put("TOTAL", total);
        return counts;
    }

    private JobApplication getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No application with id " + id + "."));
    }

    private List<ApplicationResponse> toResponses(List<JobApplication> rows) {
        List<ApplicationResponse> out = new ArrayList<>();
        for (JobApplication row : rows) {
            out.add(ApplicationResponse.from(row));
        }
        return out;
    }
}
