package com.siva.taskTracker.service;


import com.siva.taskTracker.dto.CreateCommentDto;
import com.siva.taskTracker.dto.ResponseCommentDto;
import com.siva.taskTracker.entity.Comments;
import com.siva.taskTracker.entity.Task;
import com.siva.taskTracker.entity.Team;
import com.siva.taskTracker.repository.CommentRepository;
import com.siva.taskTracker.repository.TaskRepository;
import com.siva.taskTracker.repository.TeamMemberRepository;
import com.siva.taskTracker.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;


    @Autowired
    private CommentRepository commentRepository;

    public ResponseCommentDto addCommentToTask(Long taskId, CreateCommentDto commentDto) {
        // for creating a comment we should validate the user right
        //how to validate the user belongs to the task team and also validate the task exists?
        // we can fetch the task by id and then check if the user belongs to the team associated with the task

        //getting logged in user email
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Adding comment for user: " + email);
        //fetching the task by id
        Task task=taskRepository.findById(taskId).orElseThrow(()->new RuntimeException("Task not found with id: "+taskId));
        //fetching the team associated
        Team team=task.getTeam();
        //validating the user belongs to the team
        if(!teamMemberRepository.existsByTeamAndUserEmail(team, email)){
            throw new RuntimeException("User does not belong to the team associated with the task");
        }

        //if validation passes then we can create the comment and save it
         Comments comment = new Comments();
         comment.setContent(commentDto.getContent());
         comment.setTask(task);
         commentRepository.save(comment);

        //adding to the task comments list
        task.getComments().add(comment);
        taskRepository.save(task);

        //mapping to response DTO
        ResponseCommentDto responseCommentDto = new ResponseCommentDto();
        responseCommentDto.setId(comment.getId());
        responseCommentDto.setContent(comment.getContent());
        responseCommentDto.setTaskId(task.getId());

        return responseCommentDto;


    }
}
