package com.project.alfa.common.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String message;
        
        if (exception instanceof BadCredentialsException) message = "이메일 또는 비밀번호가 올바르지 않습니다.";
        else if (exception instanceof UsernameNotFoundException) message = "존재하지 않는 이메일 주소입니다.";
        else if (exception instanceof LockedException) message = "이메일 인증이 완료되지 않은 계정입니다.";
        else if (exception instanceof InternalAuthenticationServiceException)
            message = "내부적으로 발생한 시스템 문제로 인해 요청을 처리할 수 없습니다.\n관리자에게 문의하세요.";
        else if (exception instanceof AuthenticationCredentialsNotFoundException)
            message = "인증 요청이 거부되었습니다.\n관리자에게 문의하세요.";
        else message = "알 수 없는 이유로 로그인에 실패하였습니다.\n관리자에게 문의하세요.";
        
        message = URLEncoder.encode(message, "UTF-8");
        
        response.sendRedirect("/login?error=true&message=" + message);
    }
    
}
