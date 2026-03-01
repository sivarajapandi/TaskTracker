package com.siva.taskTracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false,length = 150)
    private String name;
    @Column(name = "description")
    private String description;


    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMembers;

    @Column(name = "created_by" ,nullable = false)
    private Long createdBy;

    @Column(name = "deleted_at", updatable = true)
    private Long deletedAt;


    //as of now Team is not Having any task but in future if we want to assign task to team then we can use this relationship
    /*
    @OneToMany(mappedBy = "team")
    private List<Task> tasks;

     */









}
