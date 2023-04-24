package com.project.alfa.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Embeddable
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode
public class EmailAuth {
    
    private static final Long MAX_EXPIRE_TIME = 5L; //이메일 인증 만료 제한 시간
    
    private Boolean       auth;                     //이메일 인증 여부
    private String        authToken;                //인증 토큰(UUID)
    private LocalDateTime expireDate;               //인증 만료 시간
    
    @Builder
    public EmailAuth(String authToken) {
        this.auth = false;
        this.authToken = authToken;
        this.expireDate = LocalDateTime.now().withNano(0).plusMinutes(MAX_EXPIRE_TIME);
    }
    
    //==================== 이메일 인증 토큰 관련 메서드 ====================//
    
    /**
     * 토큰 사용
     */
    public void useToken() {
        auth = true;
    }
    
    /**
     * 토큰 변경
     *
     * @param newAuthToken - 새로운 토큰
     */
    public void updateToken(String newAuthToken) {
        auth = false;
        authToken = newAuthToken;
    }
    
}
