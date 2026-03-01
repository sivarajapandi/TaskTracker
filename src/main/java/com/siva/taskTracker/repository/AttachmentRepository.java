package com.siva.taskTracker.repository;

import com.siva.taskTracker.entity.Attachments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachments,Long> {

}
