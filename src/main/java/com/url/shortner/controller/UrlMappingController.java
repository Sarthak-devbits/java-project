package com.url.shortner.controller;

import com.url.shortner.dtos.ClickEventDTO;
import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.User;
import com.url.shortner.service.UrlMappingService;
import com.url.shortner.service.UserService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {
    private UrlMappingService urlMappingService;
    private UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String,String> request, Principal principal){
        String originalUrl= request.get("originalUrl");
        User user = userService.findUsername(principal.getName());
        UrlMappingDTO urlMappingDTO= urlMappingService.createShortUUrl(originalUrl,user);
        return ResponseEntity.ok(urlMappingDTO);
    }

    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUsersShortUrl(Principal principal){
        User user=userService.findUsername(principal.getName());
        List<UrlMappingDTO> urls=urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>> getUrlAnalytics(@PathVariable String shortUrl , @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){
        DateTimeFormatter formatter= DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start= LocalDateTime.parse(startDate,formatter);
        LocalDateTime end= LocalDateTime.parse(endDate ,formatter);
        List<ClickEventDTO> clickEventDTOS= urlMappingService.getClickEventsByDate(shortUrl,start,end);
        return ResponseEntity.ok(clickEventDTOS);
    }

    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate,Long>> getTotalClicksByDate(Principal principal, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){
        User user= userService.findUsername(principal.getName());
        DateTimeFormatter formatter= DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate start= LocalDate.parse(startDate,formatter);
        LocalDate end= LocalDate.parse(endDate ,formatter);
        Map<LocalDate,Long> totalClicks= urlMappingService.getTotalClicksByUserAndDate(user,start,end);
        return ResponseEntity.ok(totalClicks);

    }
}
