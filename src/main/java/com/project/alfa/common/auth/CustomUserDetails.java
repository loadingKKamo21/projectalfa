package com.project.alfa.common.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {
    
    private final Long    id;
    private final String  username;
    private final String  password;
    private final Boolean emailAuth;
    private final String  nickname;
    private final String  base64Image;
    private final String  role;
    
    private Map<String, Object> attributes;
    
    public CustomUserDetails(Long id,
                             String username,
                             String password,
                             Boolean emailAuth,
                             String nickname,
                             String base64Image,
                             String role,
                             Map<String, Object> attributes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.emailAuth = emailAuth;
        this.nickname = nickname;
        this.base64Image = base64Image;
        this.role = role;
        this.attributes = attributes;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> collect = new ArrayList<>();
        collect.add(new SimpleGrantedAuthority(role));
        return collect;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return emailAuth;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public String getName() {
        return username;
    }
    
}
