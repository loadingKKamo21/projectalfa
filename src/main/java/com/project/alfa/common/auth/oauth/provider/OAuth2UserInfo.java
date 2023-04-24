package com.project.alfa.common.auth.oauth.provider;

public interface OAuth2UserInfo {
    
    String getProvider();
    
    String getProviderId();
    
    String getEmail();
    
}
