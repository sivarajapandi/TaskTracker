package com.siva.taskTracker.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateAttachmentDto {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long taskId;
}
