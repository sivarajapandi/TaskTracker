package com.siva.taskTracker.controller;


import com.siva.taskTracker.dto.CreateTaskDto;
import com.siva.taskTracker.dto.ResponseCreateTaskDTO;
import com.siva.taskTracker.dto.TaskResponseDto;
import com.siva.taskTracker.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

        @Autowired
        private TaskService taskService;

        //ResponseEntity is a wrapper for HTTP response, it allows us to set status code and body of the response
        //ResponseCreateTaskDTO is a DTO class that contains the details of the created task, such as task id, task name, etc.

        @PostMapping("/createtask")
        public ResponseEntity<ResponseCreateTaskDTO> createTask(@RequestBody CreateTaskDto request) {
            // Logic to create a task
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String email=authentication.getName();

            Collection<?> roles = authentication.getAuthorities();
            System.out.println("User email: " + email);
            System.out.println("User roles: " + roles);


            //response of type ResponseCreateTaskDTO
            ResponseCreateTaskDTO response = taskService.createTaskForUser(request);

            // Return the created task with HTTP status 201 (Created)
            return ResponseEntity.status(HttpStatus.CREATED).body(response);


        }

        //endpoint to get all tasks assigned to the user
        @GetMapping("/mytasks")
        public ResponseEntity<Page<?>> getMyTasks(
                Pageable pageable) {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String email = authentication.getName();

            return ResponseEntity.ok(
                    taskService.getTasksForUser(email, pageable)
            );
        }


        //update task status after completion
        @PutMapping("/{taskId}/status")
        public ResponseEntity<?> updateTaskStatus(){
            // Logic to update task status
            return ResponseEntity.ok().build();
        }


        //endpoint to assing task to a user
        @PutMapping("/{taskId}/assign")
        public ResponseEntity<TaskResponseDto> assignTaskToUser(@PathVariable Long taskId,@RequestParam Long UserId) {
            // Logic to assign task to a user
            TaskResponseDto taskResponseDto=taskService.assignTaskToUser(taskId,UserId);
            return ResponseEntity.ok(taskResponseDto);
        }


        //endpoint to filter tasks by status
        @GetMapping("/filter")
        public ResponseEntity<Page<TaskResponseDto>> filterTasksByStatus(
                @RequestParam String status,
                Pageable pageable) {
            // Logic to filter tasks by status
           Page<TaskResponseDto> response=taskService.filterTasksByStatus(status,pageable);
            return ResponseEntity.ok(response);
        }


        //endpoint to search tasks by keyword in title or description
        @GetMapping("/search")
        public ResponseEntity<Page<TaskResponseDto>> searchTasks(@RequestParam String keyword,Pageable pageable) {
            // Logic to search tasks by keyword in title or description
            Page<TaskResponseDto> result=taskService.searchTasks(keyword,pageable);
            return ResponseEntity.ok(result);
        }








}
