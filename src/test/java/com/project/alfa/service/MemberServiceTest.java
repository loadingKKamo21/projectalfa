package com.project.alfa.service;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.common.util.EmailSender;
import com.project.alfa.common.util.FileStore;
import com.project.alfa.domain.EmailAuth;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.ProfileImage;
import com.project.alfa.repository.MemberRepository;
import com.project.alfa.service.dto.MemberEmailAuthRequestDto;
import com.project.alfa.service.dto.MemberJoinRequestDto;
import com.project.alfa.service.dto.MemberResponseDto;
import com.project.alfa.service.dto.MemberUpdateRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MemberServiceTest {
    
    @Autowired
    MemberService    memberService;
    @Autowired
    EmailSender      emailSender;
    @Autowired
    PasswordEncoder  passwordEncoder;
    @Autowired
    FileStore        fileStore;
    @Autowired
    MemberRepository memberRepository;
    @PersistenceContext
    EntityManager    em;
    
    @After
    public void clear() {
        em.flush();
        em.clear();
    }
    
    @Test
    public void 회원가입() {
        //given
        MemberJoinRequestDto dto = createJoinRequestDto("user@mail.com", "User12!@", "사용자");
        
        //when
        Long id = memberService.join(dto);
        clear();
        
        //then
        Member findMember = em.find(Member.class, id);
        
        assertEquals("Username same", dto.getUsername(), findMember.getUsername());
        assertTrue("Password match", passwordEncoder.matches(dto.getPassword(), findMember.getPassword()));
        assertEquals("Nickname same", dto.getNickname(), findMember.getNickname());
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원가입_아이디중복() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자A", true);
        em.persist(member);
        MemberJoinRequestDto dto = createJoinRequestDto("user@mail.com", "User12!@", "사용자B");
        clear();
        
        //when
        memberService.join(dto);
        clear();
        
        //then
        fail("InvalidValueException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원가입_닉네임중복() {
        //given
        Member member = createMember("userA@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        MemberJoinRequestDto dto = createJoinRequestDto("userB@mail.com", "User12!@", "사용자");
        clear();
        
        //when
        memberService.join(dto);
        clear();
        
        //then
        fail("InvalidValueException");
    }
    
    @Test
    public void 이메일인증() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", false);
        em.persist(member);
        Long    id     = member.getId();
        Boolean before = member.getEmailAuth().getAuth();
        MemberEmailAuthRequestDto dto = createEmailAuthReqeustDto(member.getUsername(),
                                                                  member.getEmailAuth().getAuthToken());
        clear();
        
        //when
        memberService.emailAuthConfirm(dto);
        Member  findMember = em.find(Member.class, id);
        Boolean after      = findMember.getEmailAuth().getAuth();
        clear();
        
        //then
        assertFalse("Email auth before emailAuthConfirm is false", before);
        assertTrue("Email auth after emailAuthConfirm is true", after);
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 이메일인증_계정엔티티조회불가() {
        //given
        MemberEmailAuthRequestDto dto = createEmailAuthReqeustDto("user@mail.com", UUID.randomUUID().toString());
        
        //when
        memberService.emailAuthConfirm(dto);
        clear();
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 인증메일재발송() {
        //given
        String beforeAuthToken = UUID.randomUUID().toString();
        Member member          = createMember("user@mail.com", "User12!@", beforeAuthToken, "사용자", true);
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        memberService.resendConfirmEmail(member.getUsername());
        Member findMember     = em.find(Member.class, id);
        String afterAuthToken = findMember.getEmailAuth().getAuthToken();
        clear();
        
        //then
        assertNotEquals("AuthToken not same", beforeAuthToken, afterAuthToken);
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 인증메일재발송_계정엔티티조회불가() {
        //given
        String email = "user@mail.com";
        
        //when
        memberService.resendConfirmEmail(email);
        clear();
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 비밀번호찾기() {
        //given
        String password = "User12!@";
        Member member   = createMember("user@mail.com", password, UUID.randomUUID().toString(), "사용자", true);
        member.authenticateEmail();
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        memberService.findPassword(member.getUsername());
        Member findMember = em.find(Member.class, id);
        clear();
        
        //then
        assertTrue("Password before findPassword match", passwordEncoder.matches(password, member.getPassword()));
        assertFalse("Password after findPassword don't match",
                    passwordEncoder.matches(password, findMember.getPassword()));
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 비밀번호찾기_계정엔티티조회불가() {
        //given
        String username = "user@mail.com";
        
        //when
        memberService.findPassword(username);
        clear();
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 비밀번호찾기_이메일인증미완료계정() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", false);
        em.persist(member);
        clear();
        
        //when
        memberService.findPassword(member.getUsername());
        clear();
        
        //then
        fail("InvalidValueException");
    }
    
    @Test
    public void 아이디찾기() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        MemberResponseDto dto = memberService.findByUsername(member.getUsername());
        clear();
        
        //then
        assertEquals("PK same", dto.getId(), id);
        assertEquals("Username same", dto.getUsername(), member.getUsername());
        assertEquals("Nickname same", dto.getNickname(), member.getNickname());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 아이디찾기_계정엔티티조회불가() {
        //given
        String username = "user@mail.com";
        
        //when
        memberService.findByUsername(username);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 회원정보수정_닉네임() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        Long id = member.getId();
        MemberUpdateRequestDto dto = createUpdateRequestDto(id,
                                                            "User12!@",
                                                            "닉네임",
                                                            null,
                                                            null,
                                                            null,
                                                            new MockMultipartFile("image", new byte[]{}));
        clear();
        
        //when
        memberService.update(dto, false);
        Member findMember = em.find(Member.class, id);
        clear();
        
        //then
        assertNotEquals("Nickname changed", member.getNickname(), findMember.getNickname());
        assertEquals("Nickname changed", findMember.getNickname(), dto.getNickname());
    }
    
    @Test
    public void 회원정보수정_서명() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        Long id = member.getId();
        MemberUpdateRequestDto dto = createUpdateRequestDto(id,
                                                            "User12!@",
                                                            null,
                                                            "서명입니다.",
                                                            null,
                                                            null,
                                                            new MockMultipartFile("image", new byte[]{}));
        clear();
        
        //when
        memberService.update(dto, false);
        Member findMember = em.find(Member.class, id);
        clear();
        
        //then
        assertNotEquals("Signature changed", member.getSignature(), findMember.getNickname());
        assertEquals("Signaure changed", findMember.getSignature(), dto.getSignature());
    }
    
    @Test
    public void 회원정보수정_비밀번호() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        Long id = member.getId();
        MemberUpdateRequestDto dto = createUpdateRequestDto(id,
                                                            "User12!@",
                                                            null,
                                                            null,
                                                            "User21@!",
                                                            "User21@!",
                                                            new MockMultipartFile("image", new byte[]{}));
        clear();
        
        //when
        memberService.update(dto, false);
        Member findMember = em.find(Member.class, id);
        clear();
        
        //then
        assertFalse("Password changed", passwordEncoder.matches(member.getPassword(), findMember.getPassword()));
        assertTrue("Password changed", passwordEncoder.matches(dto.getNewPassword(), findMember.getPassword()));
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 회원정보수정_계정엔티티조회불가() {
        //given
        Long id = 0L;
        MemberUpdateRequestDto dto = createUpdateRequestDto(id,
                                                            "User12!@",
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            new MockMultipartFile("image", new byte[]{}));
        
        //when
        memberService.update(dto, false);
        clear();
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원정보수정_비밀번호불일치() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        Long id = member.getId();
        MemberUpdateRequestDto dto = createUpdateRequestDto(id,
                                                            "User21@!",
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            new MockMultipartFile("image", new byte[]{}));
        clear();
        
        //when
        memberService.update(dto, false);
        clear();
        
        //then
        fail("InvalidValueException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원정보수정_이메일인증미완료계정() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", false);
        em.persist(member);
        Long id = member.getId();
        MemberUpdateRequestDto dto = createUpdateRequestDto(id,
                                                            "User12!@",
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            new MockMultipartFile("image", new byte[]{}));
        clear();
        
        //when
        memberService.update(dto, false);
        clear();
        
        //then
        fail("InvalidValueException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원정보수정_닉네임중복() {
        //given
        Member member1 = createMember("userA@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자A", true);
        Member member2 = createMember("userB@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자B", true);
        em.persist(member1);
        em.persist(member2);
        Long id = member1.getId();
        MemberUpdateRequestDto dto = createUpdateRequestDto(id,
                                                            "User12!@",
                                                            "사용자B",
                                                            null,
                                                            null,
                                                            null,
                                                            new MockMultipartFile("image", new byte[]{}));
        clear();
        
        //when
        memberService.update(dto, false);
        clear();
        
        //then
        fail("InvalidValueException");
    }
    
    @Test
    public void 회원탈퇴() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        ProfileImage profileImage = ProfileImage.builder()
                                                .originalFileName(null)
                                                .storeFilePath(null)
                                                .storeFilePath(null)
                                                .fileSize(null)
                                                .base64Data(null)
                                                .build();
        em.persist(profileImage);
        member.updateProfileImage(profileImage);
        Long id = member.getId();
        clear();
        
        //when
        memberService.delete("user@mail.com", "User12!@");
        Optional<Member> findMember = memberRepository.findById(id);
        clear();
        
        //then
        assertFalse("Member deleted", findMember.isPresent());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 회원탈퇴_계정엔티티조회불가() {
        //given
        String username = "user@mail.com";
        String password = "User12!@";
        
        //when
        memberService.delete(username, password);
        clear();
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원탈퇴_비밀번호불일치() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자", true);
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        memberService.delete("user@mail.com", "User21@!");
        clear();
        
        //then
        fail("InvalidValueException");
    }
    
    private Member createMember(String username, String password, String authToken, String nickname, boolean auth) {
        Member member = Member.builder()
                              .username(username)
                              .password(passwordEncoder.encode(password))
                              .emailAuth(EmailAuth.builder().authToken(authToken).build())
                              .nickname(nickname)
                              .build();
        if (auth) member.authenticateEmail();
        return member;
    }
    
    private MemberJoinRequestDto createJoinRequestDto(String username, String password, String nickname) {
        MemberJoinRequestDto dto = new MemberJoinRequestDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setNickname(nickname);
        return dto;
    }
    
    private MemberEmailAuthRequestDto createEmailAuthReqeustDto(String email, String authToken) {
        MemberEmailAuthRequestDto dto = new MemberEmailAuthRequestDto();
        dto.setEmail(email);
        dto.setAuthToken(authToken);
        dto.setCurrentTime(LocalDateTime.now());
        return dto;
    }
    
    private MemberUpdateRequestDto createUpdateRequestDto(Long id,
                                                          String password,
                                                          String nickname,
                                                          String signature,
                                                          String newPassword,
                                                          String repeatPassword,
                                                          MultipartFile file) {
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto();
        dto.setId(id);
        dto.setPassword(password);
        dto.setNickname(nickname);
        dto.setSignature(signature);
        dto.setNewPassword(newPassword);
        dto.setRepeatPassword(repeatPassword);
        dto.setProfileImage(file);
        return dto;
    }
    
}