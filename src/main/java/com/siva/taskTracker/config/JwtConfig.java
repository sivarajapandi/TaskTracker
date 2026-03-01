package com.siva.taskTracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

//what is @Configuration?
// @Configuration is an annotation used in Spring Framework to indicate that a class declares one or more
// @Bean methods and may be processed by the Spring container to generate bean definitions and service requests for those beans at runtime.
// In the context of Spring Boot, @Configuration is often used to define configuration classes that provide bean definitions and other configuration settings for the application context. It allows you to centralize and organize your application's configuration in a structured way.

// Configuration class to hold JWT related properties
// This class reads the secret key from application.properties and provides a getter for it
// The secret key is used for signing and verifying JWT tokens in the JwtUtil class

@Component
public class JwtConfig {

    //@value is used to inject the value of a property from application.properties into a field in a Spring-managed bean. In this case, it injects the value of the property "spring.jwt.secret" into the field secretKey. This allows you to externalize configuration and easily change the secret key without modifying the code.
    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String getSecretKey() {
        return secretKey;
    }


}
