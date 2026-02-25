package com.siva.taskTracker.controller;

import com.siva.taskTracker.dto.LoginRequestDto;
import com.siva.taskTracker.dto.RegisterRequest;
import com.siva.taskTracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static com.fasterxml.jackson.databind.util.ClassUtil.name;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * GET /login - Display the login form
     * Spring Security will call this when user tries to access a protected page
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Returns templates/login.html
    }

    /**
     * GET /register - Display the registration form
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // Returns templates/register.html
    }

    /**
     * POST /register - Handle user registration
     * Accepts form data and saves user to database
     */
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords don't match");
            return "register";
        }

        // Validate password length
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters");
            return "register";
        }

        try {
            // Call service to register user
            RegisterRequest registerRequest = new RegisterRequest(name, email, password, confirmPassword);
            userService.registerUser(registerRequest);

            // Redirect to login with success message
            return "redirect:/login?success";

        } catch (RuntimeException e) {
            // Handle errors (like duplicate email)
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    /**
     * POST /api/register - REST API endpoint for user registration
     * Accepts JSON request body from Postman/API clients
     */
    @PostMapping("/api/register")
    public ResponseEntity<?> registerUserApi(@RequestBody RegisterRequest request) {

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords don't match");
        }

        // Validate password length
        if (request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }

        try {
            // Call service to register user
            userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully");

        } catch (RuntimeException e) {
            // Handle errors (like duplicate email)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /dashboard - User dashboard (only accessible after login)
     * Spring Security automatically protects this via anyRequest().authenticated()
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Returns templates/dashboard.html
    }

    /**
     * POST /api/login - REST API endpoint for user login
     * Accepts JSON request body from Postman/API clients
     */
    @PostMapping("/api/login")
    public ResponseEntity<?> loginUserApi(@RequestBody LoginRequestDto loginRequestDto){

        if(userService.loginUser(loginRequestDto)){
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }

    }

    /**
     * GET /signin - A simple endpoint to test if user is authenticated (for testing purposes)
     * using jwt
     */
    @GetMapping("/signin")
    public String signIn(){
        return "signin"; // Returns templates/signin.html

    }

    @PostMapping("/signin")
    public ResponseEntity<String> signInPost(@RequestBody LoginRequestDto loginRequestDto){
        /// verify credentials and generate JWT token
        return userService.signInUser(loginRequestDto);
    }

    //endpoint to test if JWT token is valid and user is authenticated
    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("You have accessed a protected endpoint!");
    }



}


