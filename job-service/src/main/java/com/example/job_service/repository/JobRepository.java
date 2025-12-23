package com.example.job_service.repository;

import com.example.job_service.entity.Job;
import com.example.job_service.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    boolean existsByName(String name);

     List<Job> findByStatusAndScheduledTimeLessThanEqual(JobStatus jobStatus, LocalDateTime now);

   // List<Job> findByStatus(JobStatus jobStatus, LocalDateTime now);


    //  List<Job> findByStatus(JobStatus jobStatus, LocalDateTime now);
}