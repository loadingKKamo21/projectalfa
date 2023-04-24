package com.project.alfa.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberEmailAuthRequestDto {
    
    private String        email;
    private String        authToken;
    private LocalDateTime currentTime;
    
}
