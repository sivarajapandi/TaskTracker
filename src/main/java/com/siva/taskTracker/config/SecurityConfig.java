package com.siva.taskTracker.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//what is @Configuration annotation in Spring Boot?
//The @Configuration annotation in Spring Boot is used to indicate that a class declares one or more
// @Bean methods and may be processed by the Spring container to generate bean definitions and service requests for those beans at runtime. It is a part of the Spring Framework and is commonly used to define configuration classes that provide bean definitions and other configuration settings for the application context.

public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/api/register", "/api/login", "/login", "/error", "/css/**", "/js/**","/signin").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")  // Your custom login page
                        .loginProcessingUrl("/login")  // Where form posts (Spring Security handles this)
                        .usernameParameter("username")  // form field name
                        .passwordParameter("password")  // form field name
                        .defaultSuccessUrl("/dashboard", true)  // After successful login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // URL to trigger logout
                        .logoutSuccessUrl("/login?logout")  // Redirect after logout
                        .permitAll()
                );

        return http.build();


    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(11);
    }

    /**
     * Configure AuthenticationManager to use our CustomUserDetailsService and BCryptPasswordEncoder
     * @param http
     * @param passwordEncoder
     * @param userDetailsService
     * @throws Exception
     */

    //what is AuthenticationManager in Spring Security?
    //AuthenticationManager is a core component in Spring Security that is responsible for processing authentication requests. It is an interface that defines a single method, authenticate(Authentication authentication), which takes an Authentication object as input and returns a fully authenticated Authentication object if the authentication is successful. The AuthenticationManager is typically used to verify user credentials (like username and password) against a user store (like a database) and to determine if the user is authenticated and what authorities (roles) they have. It is often used in conjunction with UserDetailsService and PasswordEncoder to perform authentication in a Spring Security application.
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder passwordEncoder, UserDetailsService userDetailsService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

}
