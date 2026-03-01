package com.siva.taskTracker.dto;


import com.siva.taskTracker.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class CreateTaskDto {

    @NotBlank
    private String title;
    private String description;
    private Long assignedToUserId;
    @NotNull
    private Long teamId;

}
