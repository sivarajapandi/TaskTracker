package com.siva.taskTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ResponseCommentDto {

    private Long id;
    private String content;
    private Long taskId;
    private Long userId;
    private LocalDateTime createdAt;


}
