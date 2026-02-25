package com.siva.taskTracker.service;

import com.siva.taskTracker.dto.LoginRequestDto;
import com.siva.taskTracker.dto.RegisterRequest;
import com.siva.taskTracker.entity.Role;
import com.siva.taskTracker.entity.User;
import com.siva.taskTracker.repository.UserRepository;
import com.siva.taskTracker.util.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Autowired
    private JWTUtility jwtUtility;

    /**
     * Register a new user
     * @param name User's name
     * @param email User's email (unique)
     * @param password Plain text password (will be hashed)
     * @throws RuntimeException if email already exists
     */
    public void registerUser(RegisterRequest registerRequest) {
        // Check if email already exists
        String name = registerRequest.getName();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);
        System.out.println("Hashed password: " + hashedPassword); // Debugging line

        // Create new user
        User user = User.builder()
                .name(name)
                .email(email)
                .password(hashedPassword)  // Store HASHED password
                .role(Role.ROLE_USER)      // Default role
                .build();

        // Save to database
        userRepository.save(user);
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return userOpt.get();



    }

    /**
     * User login
     * @param loginRequestDto Login credentials (email and password)
     * @return true if login successful, false otherwise
     */
    public boolean loginUser(LoginRequestDto loginRequestDto){
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Check if user exists and password matches
        if(userOptional.isPresent()){
            User user = userOptional.get();
            // Use passwordEncoder.matches() to compare plain text with hashed password
            return passwordEncoder.matches(password, user.getPassword());
        }

        return false;
    }


    public ResponseEntity<String> signInUser(LoginRequestDto loginRequestDto) {
        //check whether user exists and password is correct
        if (loginUser(loginRequestDto)) {
            //generate JWT token and return to client
            UserDetails userDetails = findByEmail(loginRequestDto.getEmail());

            jwtUtility.generateToken(userDetails);

            System.out.println("the generated token is: " + jwtUtility.generateToken(userDetails)); // Debugging line

            return ResponseEntity.ok("Login successful. JWT token generated.");

        } else {
            throw new RuntimeException("Invalid email or password");
        }
    }
}


