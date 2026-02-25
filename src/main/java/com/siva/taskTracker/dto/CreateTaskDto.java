package com.siva.taskTracker.dto;

public class CreateTaskDto {
    private String title;
    private String description;
    private Long assignedToUserId;
    private Long teamId;

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
