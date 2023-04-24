package com.project.alfa.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableAsync
@RequiredArgsConstructor
public class EmailSender {
    
    private final JavaMailSender javaMailSender;
    
    /**
     * 이메일 전송
     *
     * @param sendType - 전송 타입 [이메일 인증, 비밀번호 찾기]
     * @param email    - 이메일 주소
     * @param content  - 내용
     */
    @Async
    public void send(SendType sendType, String email, String content) {
        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setTo(email);
        
        switch (sendType) {
            case AUTH:
                smm.setSubject("이메일 인증");
                smm.setText("계정 인증을 완료하기 위해 다음 링크를 클릭해주세요.\n" + "http://localhost:8080/confirm-email?email=" + email + "&authToken=" + content);
                break;
            case FIND_PASSWORD:
                smm.setSubject("비밀번호 찾기 결과");
                smm.setText("입력하신 정보로 찾은 계정의 임시 비밀번호는 다음과 같습니다.\n임시 비밀번호: " + content + "\n임시 비밀번호로 로그인한 다음 비밀번호를 변경해주세요.");
                break;
            default:
                return;
        }
        
        javaMailSender.send(smm);
    }
    
    public enum SendType {
        AUTH, FIND_PASSWORD
    }
    
}
