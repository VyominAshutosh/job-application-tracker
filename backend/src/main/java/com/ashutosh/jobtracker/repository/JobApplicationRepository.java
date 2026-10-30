package com.ashutosh.jobtracker.repository;

import com.ashutosh.jobtracker.entity.ApplicationStatus;
import com.ashutosh.jobtracker.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * No implementation class anywhere. Spring Data reads the method names at startup,
 * writes the queries, and hands the service a proxy that runs them.
 */
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /** Derived query: Spring Data builds the SQL from this method name alone. */
    List<JobApplication> findAllByOrderByAppliedOnDesc();

    List<JobApplication> findByStatusOrderByAppliedOnDesc(ApplicationStatus status);

    long countByStatus(ApplicationStatus status);

    /** Written by hand in JPQL, because the method name for this would be unreadable. */
    @Query("""
            SELECT a FROM JobApplication a
            WHERE LOWER(a.company) LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(a.role)    LIKE LOWER(CONCAT('%', :text, '%'))
            ORDER BY a.appliedOn DESC
            """)
    List<JobApplication> search(@Param("text") String text);
}
