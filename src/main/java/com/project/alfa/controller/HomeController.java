package com.project.alfa.controller;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.ErrorCode;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.service.MemberService;
import com.project.alfa.service.dto.MemberEmailAuthRequestDto;
import com.project.alfa.service.dto.MemberJoinRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final MemberService memberService;
    
    @GetMapping("/")
    public String index() {return "index";}
    
    @GetMapping("/login")
    public String loginPage() {return "login";}
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new MemberJoinRequestDto());
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") final MemberJoinRequestDto form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        form.setUsername(form.getUsername().toLowerCase());
        
        if (!form.getPassword().equals(form.getRepeatPassword()))   // 비밀번호 == 비밀번호 확인
            bindingResult.addError(new FieldError("form", "repeatPassword", "비밀번호가 일치하지 않습니다."));
        
        if (bindingResult.hasErrors()) return "register";
        
        try {
            memberService.join(form);
        } catch (InvalidValueException e) {
            if (e.getErrorCode() == ErrorCode.USERNAME_DUPLICATION)
                bindingResult.addError(new FieldError("form", "username", "이미 사용 중인 이메일입니다."));
            if (e.getErrorCode() == ErrorCode.NICKNAME_DUPLICATION)
                bindingResult.addError(new FieldError("form", "nickname", "이미 사용 중인 닉네임입니다."));
            if (bindingResult.hasErrors()) return "register";
        }
        
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.\n이메일 인증 완료 후 로그인해주세요.");
        
        return "redirect:/login";
    }
    
    @GetMapping("/confirm-email")
    public String confrimEmail(@ModelAttribute("form") final MemberEmailAuthRequestDto form,
                               RedirectAttributes redirectAttributes) {
        form.setCurrentTime(LocalDateTime.now());
        
        try {
            memberService.emailAuthConfirm(form);
            redirectAttributes.addAttribute("message", "이메일 인증이 완료되었습니다.\n이제 로그인해보세요!");
        } catch (EntityNotFoundException e) {
            memberService.resendConfirmEmail(form.getEmail());
            redirectAttributes.addAttribute("message", "인증이 정상적으로 완료되지 않았습니다.\n인증 메일이 재발송되었습니다. 다시 시도해주세요.");
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("form", new MemberJoinRequestDto());
        return "forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("form") final MemberJoinRequestDto form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        form.setUsername(form.getUsername().toLowerCase());
        
        if (bindingResult.hasErrors()) return "forgot-password";
        
        try {
            memberService.findPassword(form.getUsername());
        } catch (EntityNotFoundException e) {
            bindingResult.addError(new FieldError("form", "username", "해당 이메일로 등록된 계정이 없습니다."));
            return "forgot-password";
        } catch (InvalidValueException e) {
            if (e.getErrorCode() == ErrorCode.EMAIL_AUTH_NOT_COMPLETED) redirectAttributes.addFlashAttribute("message",
                                                                                                             "이메일 인증이 완료되지 않은 계정입니다.\n인증 메일이 재전송되었으니 인증 완료 후 재시도해주세요.");
            return "redirect:/login";
        }
        
        redirectAttributes.addFlashAttribute("message", "입력한 이메일로 임시 비밀번호가 전송되었습니다.\n해당 비밀번호로 로그인 후 비밀번호를 변경해주세요.");
        
        return "redirect:/login";
    }
    
}
