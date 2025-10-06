package com.url.shortner.security.jwt;

import com.url.shortner.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirastionMs;

    public String getJwtFromHeaders(HttpServletRequest request){
        String bearerToken=request.getHeader("Authorization");
        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    public String generateToken(UserDetailsImpl userDetails){
        String username=userDetails.getUsername();
        String roles=userDetails.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).collect(Collectors.joining(","));
        return Jwts.builder().subject(username).claim("roles",roles).issuedAt(new Date()).expiration(new Date(new Date().getTime()+jwtExpirastionMs)).signWith(key()).compact();
    }

    public String getUserNameFromJwtToken(String Token){
        return Jwts.parser()                     // Creates a JWT parser object, used to parse and validate JWT tokens
                .verifyWith((SecretKey) key()) // Tells the parser which key to use to verify the signature of the token.
                // Your key() method returns the HMAC SHA key.
                // Ensures the token wasn’t tampered with.
                .build()                       // Finalizes the parser configuration
                .parseSignedClaims(Token)      // Actually parses the token and checks its signature and claims.
                // If the token is invalid, expired, or modified, it will throw an exception (JwtException)
                .getPayload()                  // Extracts the payload (claims) from the token
                .getSubject();                 // Returns the 'subject' claim (in this case, the username)
    }


    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateToken(String authToken){
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
