package com.ashutosh.jobtracker.entity;

import java.util.Set;

/**
 * The five stages of the pipeline. The enum also owns the rule for which move is legal,
 * so the rule lives in one place instead of being scattered through the service.
 */
public enum ApplicationStatus {

    APPLIED,
    ASSESSMENT,
    INTERVIEW,
    OFFER,
    REJECTED;

    /** Statuses this status is allowed to move to. An empty set means the stage is final. */
    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case APPLIED    -> Set.of(ASSESSMENT, REJECTED);
            case ASSESSMENT -> Set.of(INTERVIEW, REJECTED);
            case INTERVIEW  -> Set.of(OFFER, REJECTED);
            case OFFER, REJECTED -> Set.of();
        };
    }

    public boolean canMoveTo(ApplicationStatus target) {
        return allowedNext().contains(target);
    }
}
