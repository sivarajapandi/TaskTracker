package com.siva.taskTracker.service;

import com.siva.taskTracker.dto.CreateTaskDto;
import com.siva.taskTracker.dto.ResponseCreateTaskDTO;
import com.siva.taskTracker.dto.TaskResponseDto;
import com.siva.taskTracker.entity.*;
import com.siva.taskTracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Get logged-in user
 *
 * Validate team exists
 *
 * Validate user belongs to team
 *
 * Set createdBy = currentUser
 *
 * Set assignedTo = null
 *
 * Set status = OPEN
 *
 * Save task
 *
 * Map to DTO
 *
 * Return DTO
 */



@Service
public class TaskService {
    @Autowired
    private  TaskRepository taskRepository;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  TeamRepository teamRepository;

    @Autowired
    private  TeamMemberRepository teamMemberRepository;

    //what is secur


    public ResponseCreateTaskDTO createTaskForUser(CreateTaskDto createTaskDto) {

        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Creating task for user: " + email);

        // Fetch the current user from the database using the email
        User currentUser = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        //validate team exists

        Long teamId=createTaskDto.getTeamId();
        System.out.println(teamId);

        Team team=teamRepository.findById(teamId).orElseThrow(()->new RuntimeException("Team not found with id: "+teamId));

        //validate user belongs to team

        boolean isMember= teamMemberRepository.existsByTeamIdAndUserId(teamId,currentUser.getId());

        if(!isMember){
            throw new RuntimeException("User does not belong to the team");
        }

        //after all the validation -- create a task entity and save it to the database
        Task task=new Task();

        task.setTitle(createTaskDto.getTitle());
        task.setDescription(createTaskDto.getDescription());
        task.setStatus(Status.OPEN);
        //team is an entity, we need to set the team entity to the task entity, we can fetch the team entity from the database using the team id from the createTaskDto
        task.setTeam(team);
        task.setCreatedBy(currentUser);
        task.setAssignedTo(null);

        Task savedTask=taskRepository.save(task);

        //map the saved task entity to response DTO and return it

        return mapSavedTaskToResponseDTO(savedTask);







    }

    public ResponseCreateTaskDTO mapSavedTaskToResponseDTO(Task savedTask){


        ResponseCreateTaskDTO responseCreateTaskDTO=new ResponseCreateTaskDTO();

        responseCreateTaskDTO.setId(savedTask.getId());
        responseCreateTaskDTO.setTitle(savedTask.getTitle());
        responseCreateTaskDTO.setDescription(savedTask.getDescription());
        responseCreateTaskDTO.setStatus(savedTask.getStatus());
        responseCreateTaskDTO.setTeamId(savedTask.getTeam().getId());
        responseCreateTaskDTO.setCreatedByUserId(savedTask.getCreatedBy().getId());
        responseCreateTaskDTO.setAssignedUserId(null);

        return responseCreateTaskDTO;


    }

    public Page<TaskResponseDto> getTasksForUser(String email, Pageable pageable) {
        Page<Task> taskPage = taskRepository.findByAssignedToEmail(email, pageable);
        return taskPage.map(this::mapToTaskResponseDto);
    }


    private TaskResponseDto mapToTaskResponseDto(Task task) {
        TaskResponseDto dto = new TaskResponseDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setTeamId(task.getTeam().getId());
        dto.setAssignedUserId(
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null
        );
        return dto;
    }

    public TaskResponseDto assignTaskToUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setAssignedTo(user);

        Task updatedTask = taskRepository.save(task);

        return mapToTaskResponseDto(updatedTask);

    }

    public Page<TaskResponseDto> filterTasksByStatus(String status, Pageable pageable) {

        Status taskStatus = Status.valueOf(status.toUpperCase());



        Page<Task> taskPage=taskRepository.findByStatus(status,pageable);

         return taskPage.map(this::mapToTaskResponseDto);

    }

    public Page<TaskResponseDto> searchTasks(String keyword, Pageable pageable) {
         Page<Task> taskPage = taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword,keyword,pageable);

         return taskPage.map(this::mapToTaskResponseDto);
    }

    ; // or throw an exception
}


