package com.siva.taskTracker.security;

import com.siva.taskTracker.entity.User;
import com.siva.taskTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService implements UserDetailsService
 * Spring Security calls this during login to load user from database
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Note: "username" parameter receives the email (from login form username field)
        // because our User.getUsername() returns email

        // Load user from database by email
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Return user as UserDetails (User entity implements UserDetails)
        return user;
    }
}


