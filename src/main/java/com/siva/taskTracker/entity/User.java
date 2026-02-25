package com.siva.taskTracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
    private long id;

    private String name;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "CreatedBy")
    private List<Task> createdTasks;

    @OneToMany(mappedBy = "assignedTo")
    private List<Task> assignedTasks;

    @OneToMany(mappedBy="commentedByUser")
    private List<Comment> comments;

    @OneToMany(mappedBy="uploadedByUser")
    private List<Attachment> attachments;

    @OneToMany(mappedBy ="user")
    Private List<TeamMember> teamMembers;



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





}
