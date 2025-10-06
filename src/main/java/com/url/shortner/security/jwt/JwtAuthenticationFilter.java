package com.url.shortner.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtTokenProvider; // Utility class to generate, parse, and validate JWT tokens

    @Autowired
    private UserDetailsService userDetailsService; // Loads user details (from DB or other source) by username

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1️⃣ Extract JWT token from the Authorization header
            String jwt = jwtTokenProvider.getJwtFromHeaders(request);

            // 2️⃣ If JWT exists and is valid
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {

                // 3️⃣ Extract username from JWT
                String username = jwtTokenProvider.getUserNameFromJwtToken(jwt);

                // 4️⃣ Load full user details from database using username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5️⃣ If userDetails exists, create an Authentication object
                if (userDetails != null) {
                    // UsernamePasswordAuthenticationToken is a Spring Security object that represents an authenticated user.
                    // Create authentication object containing user details and authorities (roles)
                    // Second parameter (credentials) is null because JWT is already verified
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // 6️⃣ Optional: Add extra details from request like IP address or session ID
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 7️⃣ Set the authentication object in Spring Security context
                    // This marks the user as "authenticated" for the current request
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            // Handle exceptions (token invalid, parsing failed, etc.)
            e.printStackTrace();
            // Optionally, you can throw RuntimeException or send an error response
        }

        // 8️⃣ Continue the filter chain (important!)
        // This ensures the request goes to the next filter or eventually the controller
        filterChain.doFilter(request, response);
    }
}
