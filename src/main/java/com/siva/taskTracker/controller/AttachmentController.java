package com.siva.taskTracker.controller;

import com.siva.taskTracker.dto.CreateAttachmentDto;
import com.siva.taskTracker.dto.ResponseAttachmentDto;
import com.siva.taskTracker.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    // This controller will handle all the endpoints related to attachments on tasks.
    // It will allow users to upload attachments to tasks, view attachments, and delete attachments if necessary.

    //endpoint to upload an attachment to a task
    // We will use MultipartFile to handle file uploads in Spring Boot
    // We will also need to create a service to handle the file storage and retrieval logic, but for now, we will just create the endpoint and return a success message.
    @PostMapping("/upload")
    public ResponseAttachmentDto uploadAttachment(@PathVariable Long taskId, CreateAttachmentDto attachmentDto) {

        // Logic to upload an attachment to a task
        ResponseAttachmentDto response=attachmentService.createAttachmentForTask(taskId, attachmentDto);

        return response;
    }


}
