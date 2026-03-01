package com.siva.taskTracker.dto;

import com.siva.taskTracker.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter


public class ResponseCreateTaskDTO {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private Long createdByUserId;
    private Long assignedUserId;
    private Long teamId;

}
