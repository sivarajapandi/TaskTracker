package com.siva.taskTracker.filter;

import com.siva.taskTracker.util.JWTUtility;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtility jwtUtility;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwtToken = request.getHeader("Authorization");
        System.out.println("JWT Token:"+ jwtToken);

        if(jwtToken == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing JWT token");
            return;
        }

        // Here you would typically validate the JWT token and set the authentication in the security context
        Claims claims= jwtUtility.validateToken(jwtToken);

        System.out.println(claims);

        // If token is valid, continue with the filter chain
        filterChain.doFilter(request, response);

    }

    // This method is used to specify which endpoints should be excluded from JWT filtering. In this case, we want to allow unauthenticated access to the /login and /register endpoints so that users can log in and register without needing a token.

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Skip filtering for /login and /register endpoints
        //what is getRequestURI()?
        // The getRequestURI() method in the HttpServletRequest interface is used to retrieve the part of the request URL that indicates the path of the resource being requested. It returns a String representing the URI of the request, which typically includes the context path and the servlet path. For example, if the request URL is http://localhost:8080/login, getRequestURI() would return "/login". This method is useful for determining which endpoint is being accessed and can be used to conditionally
        String path = request.getRequestURI();
        // You can add more endpoints to this condition as needed (e.g., /signin)
        return path.equals("/login") || path.equals("/register") || path.equals("/signin");
    }
}
