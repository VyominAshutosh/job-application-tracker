package com.ashutosh.jobtracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * One row of the job_application table. Hibernate reads these annotations
 * and creates the table at startup (ddl-auto=update).
 */
@Entity
@Table(name = "job_application")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String company;

    @Column(nullable = false, length = 120)
    private String role;

    @Column(length = 120)
    private String location;

    /** Where the application came from: Campus, LinkedIn, Referral, Company site. */
    @Column(length = 60)
    private String source;

    @Column(name = "applied_on", nullable = false)
    private LocalDate appliedOn;

    /** Empty until an interview is scheduled. */
    @Column(name = "interview_date")
    private LocalDate interviewDate;

    /** Stored as the name ("APPLIED"), not the position number. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(length = 500)
    private String notes;

    /** JPA needs a no-argument constructor to build the object before filling the fields. */
    protected JobApplication() {
    }

    public JobApplication(String company, String role, String location, String source,
                          LocalDate appliedOn, LocalDate interviewDate, String notes) {
        this.company = company;
        this.role = role;
        this.location = location;
        this.source = source;
        this.appliedOn = appliedOn;
        this.interviewDate = interviewDate;
        this.notes = notes;
        this.status = ApplicationStatus.APPLIED;
    }

    public Long getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDate getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(LocalDate appliedOn) {
        this.appliedOn = appliedOn;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
