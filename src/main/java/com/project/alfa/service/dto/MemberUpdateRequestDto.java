package com.project.alfa.service.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Pattern;

@Getter
@Setter
public class MemberUpdateRequestDto {
    
    private final String EMAIL_REGEX     = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"; //Regular Expression by RFC 5322 for Email Validation
    private final String PASSWORD_REGEX  = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[`~!@#$%^&*\\\\()\\\\-_\\\\=+\\\\[{\\\\]}\\\\|;\\\\:'\\\\\\\"\\\\,<\\\\.>\\\\/?]).{8,32}$";
    private final String NICKNAME_REGEX  = "^[a-zA-Z0-9가-힣]{1,20}$";
    private final String SIGNATURE_REGEX = "^.{0,100}$";
    
    private Long id;
    
    @Pattern(message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자", regexp = PASSWORD_REGEX)
    private String password;
    
    @Pattern(message = "영문, 숫자, 한글, 1~20자", regexp = NICKNAME_REGEX)
    private String nickname;
    
    @Pattern(message = "최대 100자까지 가능합니다.", regexp = SIGNATURE_REGEX)
    private String signature;
    
    @Pattern(message = "영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자", regexp = PASSWORD_REGEX)
    private String newPassword;
    
    private String repeatPassword;
    
    private MultipartFile profileImage;
    
}
