package com.project.alfa.common.auth;

import com.project.alfa.domain.Member;
import com.project.alfa.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final MemberRepository memberRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username.toLowerCase())
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                "Could not found user by username: " + username));
        return new CustomUserDetails(member.getId(),
                                     member.getUsername(),
                                     member.getPassword(),
                                     member.getEmailAuth().getAuth(),
                                     member.getNickname(),
                                     member.getProfileImage().getBase64Data(),
                                     member.getRole().value());
    }
    
}
