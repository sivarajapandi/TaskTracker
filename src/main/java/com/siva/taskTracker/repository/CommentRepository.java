package com.siva.taskTracker.repository;

import com.siva.taskTracker.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comments, Long> {



}
