package com.siva.taskTracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//UserDetails is an ID card format that Spring Security uses to authenticate and authorize users. By implementing UserDetails, we can integrate our User entity with Spring Security's authentication mechanism, allowing us to manage user authentication and authorization seamlessly within our application.

//spring security says give me the user information in my format and I will handle the authentication and authorization process. By implementing UserDetails, we can provide the necessary user information (like username, password, roles, etc.) in a way that Spring Security understands, allowing it to perform authentication and authorization based on that information.
public class User  implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name ="role",length = 25)
    private Role role;


    @OneToMany(mappedBy = "CreatedBy")
    private List<Task> createdTasks;


    @OneToMany(mappedBy = "assignedTo")
    private List<Task> assignedTasks;

    @OneToMany(mappedBy="commentedByUser")
    private List<Comments> comments;

    @OneToMany(mappedBy="uploadedByUser")
    private List<Attachments> attachments;

    @OneToMany(mappedBy ="user")
    private List<TeamMember> teamMembers;


    //how to default value for createdAt and updatedAt
    //
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;



    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }





}
