package com.siva.taskTracker.controller;


import com.siva.taskTracker.dto.CreateCommentDto;
import com.siva.taskTracker.dto.ResponseCommentDto;
import com.siva.taskTracker.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/tasks/comments")
public class CommentController {

    // This controller will handle all the endpoints related to comments on tasks.
    // It will allow users to add comments to tasks, view comments, and delete comments if necessary.

    @Autowired
        private CommentService commentService;

    //endpoint to add a comment to a task
    @PostMapping("{taskId}/addComments")
    public ResponseEntity<ResponseCommentDto> addComment(@PathVariable Long takId, @RequestBody CreateCommentDto commentDto) {
        // Logic to add a comment to a task
        commentService.addCommentToTask(takId, commentDto);

    }




}
