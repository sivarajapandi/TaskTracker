package com.siva.taskTracker.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Table(name = "comments")
@AllArgsConstructor
@NoArgsConstructor

@Getter
@Setter
@Entity
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 2000)
    private String content;

    @ManyToOne
    @JoinColumn(name ="task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name= "user_id")
    private User user;

    private LocalDateTime createdAt;

    @Column(name = "commentedByUser")
    private User commentedByUser;
}
