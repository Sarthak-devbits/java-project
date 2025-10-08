package com.url.shortner.service;

import com.url.shortner.dtos.LoginRequest;
import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.User;
import com.url.shortner.repository.UserRepository;
import com.url.shortner.security.jwt.JwtAuthenticationResposne;
import com.url.shortner.security.jwt.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/*
*
* 1️⃣ User sends login request
   POST /login
   {
       "username": "john",
       "password": "1234"
   }

            |
            v

2️⃣ Spring calls your method: authenticateUser(loginRequest)
   - Extracts username & password from LoginRequest

            |
            v

3️⃣ Authenticate credentials
   Authentication authentication = authenticationManager.authenticate(
       new UsernamePasswordAuthenticationToken(username, password)
   );
   - AuthenticationManager checks:
     • Is username valid?
     • Does password match?
   - If correct → returns Authentication object with user details
   - If wrong → throws AuthenticationException (401 Unauthorized)

            |
            v

4️⃣ Store authentication in SecurityContext
   SecurityContextHolder.getContext().setAuthentication(authentication);
   - Saves info about **current logged-in user** in thread-local storage
   - Spring Security can now check user roles/authorities anytime in this request

            |
            v

5️⃣ Generate JWT token
   UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
   String jwt = jwtUtils.generateToken(userDetails);
   - Token contains user info (like username, roles) and expiry
   - JWT is stateless → server doesn’t need session storage

            |
            v

6️⃣ Return JWT to client
   return new JwtAuthenticationResponse(jwt);
   - Client stores JWT (e.g., localStorage)
   - Client sends JWT in `Authorization: Bearer <token>` for future requests

            |
            v

7️⃣ Future requests
   - Spring Security parses JWT
   - Loads user info into SecurityContextHolder automatically
   - Checks roles/permissions for secured endpoints

* */


@Service
@AllArgsConstructor
public class UserService {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;

    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findUsername(String name){
        return userRepository.findByUsername(name).orElseThrow(() -> new UsernameNotFoundException("User not found with username "+name));
    }

    public JwtAuthenticationResposne authenticateUser(LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails= (UserDetailsImpl) authentication.getPrincipal();
        String jwt= jwtUtils.generateToken(userDetails);
        return new JwtAuthenticationResposne(jwt);
    }


}
