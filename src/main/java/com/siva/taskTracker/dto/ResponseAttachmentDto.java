package com.siva.taskTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class ResponseAttachmentDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long taskId;
    private String uploadedByUserEmail;
    private LocalDateTime uploadedAt;

}
