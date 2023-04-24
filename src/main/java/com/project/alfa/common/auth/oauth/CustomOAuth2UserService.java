package com.project.alfa.common.auth.oauth;

import com.project.alfa.common.auth.CustomUserDetails;
import com.project.alfa.common.auth.oauth.provider.GoogleUserInfo;
import com.project.alfa.common.auth.oauth.provider.OAuth2UserInfo;
import com.project.alfa.domain.EmailAuth;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.ProfileImage;
import com.project.alfa.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final String NUM          = "0123456789";
    private final String LOWER_ALPHA  = "abcdefghijklmnopqrstuvwxyz";
    private final String UPPER_ALPHA  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String SPECIAL_CHAR = "`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder  passwordEncoder;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getClientName().equals("Google"))
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        
        String username = oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId();
        String nickname;
        do {
            nickname = oAuth2User.getAttribute("name") + "_" + randomAlphaNumeric(10);
        } while (memberRepository.existsByNickname(nickname));
        
        Member member = null;
        if (!memberRepository.existsByUsername(username)) {
            member = Member.builder()
                           .username(username)
                           .password(passwordEncoder.encode(randomPassword(20)))
                           .emailAuth(EmailAuth.builder().authToken(UUID.randomUUID().toString()).build())
                           .nickname(nickname)
                           .profileImage(ProfileImage.builder()
                                                     .originalFileName(null)
                                                     .storeFileName(null)
                                                     .storeFilePath(null)
                                                     .fileSize(null)
                                                     .base64Data(null)
                                                     .build())
                           .build();
            member.setOAuthInfo(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getProviderId());
            member.authenticateEmail();
            memberRepository.save(member);
        } else member = memberRepository.findByUsername(username).get();
        
        return new CustomUserDetails(member.getId(),
                                     member.getUsername(),
                                     member.getPassword(),
                                     member.getEmailAuth().getAuth(),
                                     member.getNickname(),
                                     member.getProfileImage().getBase64Data(),
                                     member.getRole().value(),
                                     oAuth2User.getAttributes());
    }
    
    private String randomAlphaNumeric(int count) {
        StringBuilder sb   = new StringBuilder();
        Random        rand = new Random();
        
        int n = 0, l = 0, u = 0;
        
        while (!(n + l + u == count)) {
            n = rand.nextInt(count) + 1;
            l = rand.nextInt(count) + 1;
            u = rand.nextInt(count) + 1;
        }
        
        while (n > 0 && n-- != 0) sb.append(NUM.charAt(rand.nextInt(NUM.length() - 1)));
        while (l > 0 && l-- != 0) sb.append(LOWER_ALPHA.charAt(rand.nextInt(LOWER_ALPHA.length() - 1)));
        while (u > 0 && u-- != 0) sb.append(UPPER_ALPHA.charAt(rand.nextInt(UPPER_ALPHA.length() - 1)));
        
        List<String> generatedChars = Arrays.asList(sb.toString().split(""));
        
        Collections.shuffle(generatedChars);
        
        String shuffleStr = "";
        for (String string : generatedChars) shuffleStr += string;
        
        return shuffleStr;
    }
    
    private String randomPassword(int count) {
        StringBuilder sb   = new StringBuilder();
        Random        rand = new Random();
        
        int n = 0, l = 0, u = 0, s = 0;
        
        while (!(n + l + u + s == count)) {
            n = rand.nextInt(count) + 1;
            l = rand.nextInt(count) + 1;
            u = rand.nextInt(count) + 1;
            s = rand.nextInt(count) + 1;
        }
        
        while (n > 0 && n-- != 0) sb.append(NUM.charAt(rand.nextInt(NUM.length() - 1)));
        while (l > 0 && l-- != 0) sb.append(LOWER_ALPHA.charAt(rand.nextInt(LOWER_ALPHA.length() - 1)));
        while (u > 0 && u-- != 0) sb.append(UPPER_ALPHA.charAt(rand.nextInt(UPPER_ALPHA.length() - 1)));
        while (s > 0 && s-- != 0) sb.append(SPECIAL_CHAR.charAt(rand.nextInt(SPECIAL_CHAR.length() - 1)));
        
        List<String> generatedChars = Arrays.asList(sb.toString().split(""));
        
        Collections.shuffle(generatedChars);
        
        String shuffleStr = "";
        for (String string : generatedChars) shuffleStr += string;
        
        return shuffleStr;
    }
    
}
