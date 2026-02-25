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
    private Long id;

    private String name;

    private String description;


    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMembers;


    //as of now Team is not Having any task but in future if we want to assign task to team then we can use this relationship
    /*
    @OneToMany(mappedBy = "team")
    private List<Task> tasks;

     */









}
