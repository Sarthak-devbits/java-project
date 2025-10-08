package com.url.shortner.service;

import com.url.shortner.dtos.ClickEventDTO;
import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.ClickEvent;
import com.url.shortner.models.UrlMapping;
import com.url.shortner.models.User;
import com.url.shortner.repository.ClickEventRepository;
import com.url.shortner.repository.UrlMappingRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UrlMappingService {
    private UrlMappingRepository urlMappingRepository;
    private ClickEventRepository clickEventRepository;

    public UrlMappingDTO createShortUUrl(String originalUrl, User user) {
        String shortUrl=generateShortUrl();

        UrlMapping urlMapping= new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        urlMappingRepository.save(urlMapping);
        return convertToDTO(urlMapping);
    }

    public UrlMappingDTO convertToDTO(UrlMapping urlMapping){
        UrlMappingDTO urlMappingDTO= new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setCreateDate(urlMapping.getCreatedDate());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDTO;
    }

    private String generateShortUrl() {
        String characters="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random= new Random();
        StringBuilder shortUrl= new StringBuilder(8);
        for(int i=0;i<8;i++){
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    public List<UrlMappingDTO> getUrlsByUser(User user) {
        return urlMappingRepository.findByUser(user).stream().map(this::convertToDTO).toList();
    }

    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
          UrlMapping urlMapping= urlMappingRepository.findByShortUrl(shortUrl);
          if(urlMapping!=null){
             return clickEventRepository.
                     findByUrlMappingAndClickDateBetween(urlMapping,start,end)
                     .stream()
                     .collect(Collectors.groupingBy(clickEvent -> clickEvent.getClickDate().toLocalDate(),Collectors.counting()))
                     .entrySet().stream().map(localDateLongEntry -> {
                         ClickEventDTO clickEventDTO= new ClickEventDTO();
                         clickEventDTO.setClickDate(localDateLongEntry.getKey());
                         clickEventDTO.setCount(localDateLongEntry.getValue());
                         return  clickEventDTO;
                     }).collect(Collectors.toList());
          }
          return null;
    }


    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        List<UrlMapping> urlMapping= urlMappingRepository.findByUser(user);
        List<ClickEvent> clientEvents=clickEventRepository.findByUrlMappingInAndClickDateBetween(urlMapping,start.atStartOfDay(),end.plusDays(1).atStartOfDay());
        return clientEvents.stream()
                .collect(Collectors.groupingBy(clickEvent -> clickEvent.getClickDate().toLocalDate(),Collectors.counting()));
    }

    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping=urlMappingRepository.findByShortUrl(shortUrl);
        if(urlMapping!=null){
            urlMapping.setClickCount(urlMapping.getClickCount() + 1);
            urlMappingRepository.save(urlMapping);

            // Record the Click Event
             ClickEvent clickEvent= new ClickEvent();
             clickEvent.setClickDate(LocalDateTime.now());
             clickEvent.setUrlMapping(urlMapping);
             clickEventRepository.save(clickEvent);
        }
        return urlMapping;
    }
}
