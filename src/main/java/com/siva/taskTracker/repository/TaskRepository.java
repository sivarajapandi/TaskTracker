package com.siva.taskTracker.repository;

import com.siva.taskTracker.dto.TaskResponseDto;
import com.siva.taskTracker.entity.Status;
import com.siva.taskTracker.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task , Long > {

    Page<Task> findByTeamIdAndDeletedAtIsNull(Long teamId, org.springframework.data.domain.Pageable pageable);
    Page<Task> findByAssignedToIdAndDeletedAtIsNull(Long userId, org.springframework.data.domain.Pageable pageable);

    Page<Task> findByTeamIdAndStatusAndDeletedAtIsNull(
            Long teamId,
            Status status,
            Pageable pageable
    );


    Page<Task> findByAssignedToEmail(String email, Pageable pageable);


    Page<Task> findByStatus(String status, Pageable pageable);

    // TaskRepository.java
    Page<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);
}
