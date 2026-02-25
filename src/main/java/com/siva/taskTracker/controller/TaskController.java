package com.siva.taskTracker.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

        public String createTask() {
            // Logic to create a task
            return "Task created successfully!";
        }

}
