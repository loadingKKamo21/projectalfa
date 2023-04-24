package com.project.alfa.controller;

import com.project.alfa.common.auth.CustomUserDetails;
import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.ErrorCode;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.service.MemberService;
import com.project.alfa.service.dto.CommentResponseDto;
import com.project.alfa.service.dto.MemberResponseDto;
import com.project.alfa.service.dto.MemberUpdateRequestDto;
import com.project.alfa.service.dto.PostListResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService         memberService;
    private final AuthenticationManager authenticationManager;
    
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("member", memberService.findByUsername(userDetails.getUsername()));
        return "members/profile";
    }
    
    @GetMapping("/profile-update")
    public String profileUpdatePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("member", memberService.findByUsername(userDetails.getUsername()));
        model.addAttribute("form", new MemberUpdateRequestDto());
        return "members/profile-update";
    }
    
    @PostMapping("/profile-update")
    public String profileUpdate(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("form") final MemberUpdateRequestDto form,
                                BindingResult bindingResult,
                                @RequestParam(required = false, name = "deleteImg") boolean deleteImg,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        MemberResponseDto memberDto         = memberService.findByUsername(userDetails.getUsername());
        
        Boolean newPasswordInput  = null;
        Boolean newPasswordRepeat = null;
        
        if (customUserDetails.getAttributes() == null) {
            newPasswordInput = form.getNewPassword() != null && !form.getNewPassword().trim().isEmpty();
            
            if (newPasswordInput)   //비밀번호 변경 시
                newPasswordRepeat = form.getNewPassword().equals(form.getRepeatPassword()); //비밀번호 확인 값과 일치 여부 확인
            
            if (bindingResult.hasFieldErrors("newPassword") || (newPasswordInput && !newPasswordRepeat)) {  //새로운 비밀번호 및 확인 값 에러 발생 시
                bindingResult.addError(new FieldError("form",
                                                      "password",
                                                      "새로운 비밀번호 확인: 영문 대/소문자, 숫자, 특수문자 각 최소 1개 이상 포함, 8~32자"));
                model.addAttribute("member", memberDto);
                return "members/profile-update";
            }
        } else form.setPassword(customUserDetails.getPassword());
        
        try {
            memberService.update(form, deleteImg);
            if (customUserDetails.getAttributes() == null)
                if (newPasswordInput && newPasswordRepeat) {    //비밀번호 변경 성공 시 로그아웃
                    redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.\n변경된 비밀번호로 다시 로그인해주세요.");
                    return "redirect:/login?logout";
                }
            //        } catch (EntityNotFoundException e) {
            //            redirectAttributes.addFlashAttribute("message", "존재하지 않는 계정입니다.");
            //            return "redirect:/login?logout";
        } catch (InvalidValueException e) {
            if (e.getErrorCode() == ErrorCode.PASSWORD_DO_NOT_MATCH)
                bindingResult.addError(new FieldError("form", "password", "계정 비밀번호 불일치"));
            if (e.getErrorCode() == ErrorCode.EMAIL_AUTH_NOT_COMPLETED) {
                redirectAttributes.addFlashAttribute("message",
                                                     "이메일 인증이 완료되지 않은 계정입니다.\n인증 메일이 재전송되었으니 인증 완료 후 재시도해주세요.");
                return "redirect:/login?logout";
            }
            if (e.getErrorCode() == ErrorCode.NICKNAME_DUPLICATION)
                bindingResult.addError(new FieldError("form", "nickname", "닉네임 중복"));
        }
        
        if (bindingResult.hasErrors()) {    //필드 에러 확인
            model.addAttribute("member", memberDto);
            return "members/profile-update";
        }
        
        if (customUserDetails.getAttributes() == null)
            if (!form.getNickname().equals(customUserDetails.getNickname()) || (!form.getProfileImage()
                                                                                     .isEmpty() || deleteImg)) {    //닉네임 or 프로필 사진이 변경된 경우
                memberDto = memberService.findByUsername(userDetails.getUsername());
                CustomUserDetails updatedUserDetails = new CustomUserDetails(customUserDetails.getId(),
                                                                             customUserDetails.getUsername(),
                                                                             customUserDetails.getPassword(),
                                                                             customUserDetails.getEmailAuth(),
                                                                             memberDto.getNickname(),
                                                                             memberDto.getImage64(),
                                                                             customUserDetails.getRole());
                UsernamePasswordAuthenticationToken updatedAuthentication = new UsernamePasswordAuthenticationToken(
                        updatedUserDetails,
                        form.getPassword(),
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext()
                                     .setAuthentication(authenticationManager.authenticate(updatedAuthentication));
            }
        
        return "redirect:/members/profile";
    }
    
    @PostMapping("/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("form") final MemberUpdateRequestDto form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (((CustomUserDetails) userDetails).getAttributes() != null) form.setPassword(userDetails.getPassword());
        
        try {
            memberService.delete(userDetails.getUsername(), form.getPassword());
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.\n이용해주셔서 감사합니다.");
            //        } catch (EntityNotFoundException e) {
            //            redirectAttributes.addFlashAttribute("message", "존재하지 않는 계정입니다.");
        } catch (InvalidValueException e) {
            if (e.getErrorCode() == ErrorCode.PASSWORD_DO_NOT_MATCH)
                bindingResult.addError(new FieldError("form", "password", "계정 비밀번호 불일치"));
            model.addAttribute("member", memberService.findByUsername(userDetails.getUsername()));
            return "members/profile-update";
        }
        
        return "redirect:/login?logout";
    }
    
    @GetMapping("/activity")
    public String myActivityPage(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(defaultValue = "0") int pageP,
                                 @RequestParam(defaultValue = "0") int pageC,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String username = ((CustomUserDetails) userDetails).getUsername();
        
        Pageable pageableP = PageRequest.of(pageP, 10);
        Pageable pageableC = PageRequest.of(pageC, 10);
        
        try {
            model.addAttribute("posts",
                               memberService.myActivityContents(username, pageableP, PostListResponseDto.class));
            model.addAttribute("comments",
                               memberService.myActivityContents(username, pageableC, CommentResponseDto.class));
        } catch (InvalidValueException e) {
            redirectAttributes.addFlashAttribute("message", "나의 활동 정보를 조회할 수 없습니다.\n관리자에게 문의하세요.");
            return "redirect:/members/profile";
        }
        
        return "members/activity";
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("message", "존재하지 않는 계정입니다.");
        RedirectView redirectView = new RedirectView("/login?logout");
        redirectView.setExposeModelAttributes(false);
        mav.setView(redirectView);
        return mav;
    }
    
}
