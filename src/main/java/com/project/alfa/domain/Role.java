package com.project.alfa.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    
    ADMIN("ROLE_ADMIN"), USER("ROLE_USER");
    
    private final String role; //계정 유형
    
    public String value() {
        return role;
    }
    
}
