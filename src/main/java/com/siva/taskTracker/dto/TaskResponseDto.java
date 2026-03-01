package com.siva.taskTracker.dto;

import com.siva.taskTracker.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private Long assignedUserId;
    private Long teamId;

    // Getters and Setters

}
