package com.siva.taskTracker.service;

import com.siva.taskTracker.dto.CreateAttachmentDto;
import com.siva.taskTracker.dto.ResponseAttachmentDto;
import com.siva.taskTracker.entity.Attachments;
import com.siva.taskTracker.entity.Task;
import com.siva.taskTracker.entity.Team;
import com.siva.taskTracker.entity.User;
import com.siva.taskTracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

public class AttachmentService {


    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserRepository userRepository;




    public ResponseAttachmentDto createAttachmentForTask(Long taskId, CreateAttachmentDto attachmentDto) {
        // Logic to create an attachment for a task
        // This will involve saving the attachment details in the database and storing the file in a file storage system (like AWS S3, local file system, etc.)
        // For now, we will just save the attachment details in the database and return a success message.

        // We will need to create an Attachment entity and map it to the CreateAttachmentDto, then save it using the attachmentRepository.

        //same user validation logic
        //getting logged in user email
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Adding comment for user: " + email);
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found for the email"));
        //fetching the task by id
        Task task=taskRepository.findById(taskId).orElseThrow(()->new RuntimeException("Task not found with id: "+taskId));
        //fetching the team associated
        Team team=task.getTeam();
        //validating the user belongs to the team
        if(!teamMemberRepository.existsByTeamAndUserEmail(team, email)){
            throw new RuntimeException("User does not belong to the team associated with the task");
        }

        Attachments attachmentObj=new Attachments();

        attachmentObj.setCreatedAt(LocalDateTime.now());
        attachmentObj.setTask(task);
        attachmentObj.setFileName(attachmentDto.getFileName());
        attachmentObj.setUploadedByUser(user);
        attachmentObj.setFileUrl(attachmentDto.getFileUrl());

        attachmentRepository.save(attachmentObj);

        taskRepository.save(task);

        //mapping to response DTO
        // We will need to create a ResponseAttachmentDto and map the saved attachment entity to it, then return it as the response of the API endpoint.
        ResponseAttachmentDto responseAttachmentDto=new ResponseAttachmentDto();
        responseAttachmentDto.setId(attachmentObj.getId());
        responseAttachmentDto.setFileName(attachmentObj.getFileName());
        responseAttachmentDto.setFileUrl(attachmentObj.getFileUrl());
        responseAttachmentDto.setUploadedByUserEmail(attachmentObj.getUploadedByUser().getEmail());
        responseAttachmentDto.setUploadedAt(attachmentObj.getCreatedAt());

        return responseAttachmentDto;

    }
}
