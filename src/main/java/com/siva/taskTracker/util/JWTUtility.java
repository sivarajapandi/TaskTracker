package com.siva.taskTracker.util;

import com.siva.taskTracker.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JWTUtility {

    @Autowired
    private JwtConfig jwtConfig;


    public JWTUtility(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }


    //generate JWT token
    //token should be generated only after successful authentication (in AuthController.login method)
    public String generateToken(UserDetails userDetails) {
        //TODO: implement JWT token generation logic
        // You can use a library like io.jsonwebtoken.Jwts to create JWT tokens
        // The token should include the username (email) and an expiration time
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .claim("ADMIN", userDetails.getAuthorities())
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecretKey()) // Use a secure key in production
                .compact();

        //what is compact()?
        // The compact() method in the Jwts.builder() is used to build the JWT token and return it as a compact, URL-safe string. It finalizes the construction of the JWT


    }


    public String extractUsername(String token) {
        //TODO: implement logic to extract username (email) from JWT token
        return "String name Extracted from token";
    }


    public Claims validateToken(String jwtToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(jwtConfig.getSecretKey()).build().parseClaimsJws(jwtToken).getBody();

        System.out.println(claims);
        System.out.println("Token is valid. Username: " + claims.getSubject());

        return claims;
    }



}
