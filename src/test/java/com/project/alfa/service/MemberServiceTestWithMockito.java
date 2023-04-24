package com.project.alfa.service;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.common.util.EmailSender;
import com.project.alfa.common.util.FileStore;
import com.project.alfa.domain.EmailAuth;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.ProfileImage;
import com.project.alfa.domain.Role;
import com.project.alfa.repository.CommentRepository;
import com.project.alfa.repository.MemberRepository;
import com.project.alfa.repository.PostRepository;
import com.project.alfa.service.dto.MemberEmailAuthRequestDto;
import com.project.alfa.service.dto.MemberJoinRequestDto;
import com.project.alfa.service.dto.MemberUpdateRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest(MemberService.class)
public class MemberServiceTestWithMockito {
    
    MemberService     memberService;
    EmailSender       emailSender;
    PasswordEncoder   passwordEncoder;
    FileStore         fileStore;
    MemberRepository  memberRepository;
    PostRepository    postRepository;
    CommentRepository commentRepository;
    
    @Before
    public void setup() {
        memberRepository = mock(MemberRepository.class);
        emailSender = mock(EmailSender.class);
        passwordEncoder = mock(PasswordEncoder.class);
        fileStore = mock(FileStore.class);
        memberRepository = mock(MemberRepository.class);
        postRepository = mock(PostRepository.class);
        commentRepository = mock(CommentRepository.class);
        memberService = PowerMockito.spy(new MemberService(memberRepository,
                                                           emailSender,
                                                           passwordEncoder,
                                                           fileStore,
                                                           postRepository,
                                                           commentRepository));
    }
    
    @Test
    public void 회원가입() throws Exception {
        //given
        MemberJoinRequestDto dto = createJoinRequestDto("user@mail.com", "User12!@", "사용자");
        
        Member member = mock(Member.class);
        
        //when
        memberService.join(dto);
        
        //then
        PowerMockito.verifyPrivate(memberService, times(1)).invoke("validateDuplicate", anyString(), anyString());
        verify(memberRepository, times(1)).existsByUsername(anyString());
        verify(memberRepository, times(1)).existsByNickname(anyString());
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(emailSender, times(1)).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원가입_아이디중복() throws Exception {
        //given
        MemberJoinRequestDto dto = createJoinRequestDto("user@mail.com", "User12!@", "사용자");
        
        when(memberRepository.existsByUsername(anyString())).thenReturn(true);
        
        //when
        memberService.join(dto);
        
        //then
        PowerMockito.verifyPrivate(memberService, times(1)).invoke("validateDuplicate", anyString(), anyString());
        verify(memberRepository, times(1)).existsByUsername(anyString());
        verify(memberRepository, never()).existsByUsername(anyString());
        verify(memberRepository, never()).save(any(Member.class));
        verify(emailSender, never()).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test(expected = InvalidValueException.class)
    public void 회원가입_닉네임중복() throws Exception {
        //given
        MemberJoinRequestDto dto = createJoinRequestDto("user@mail.com", "User12!@", "사용자");
        
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);
        
        //when
        memberService.join(dto);
        
        //then
        PowerMockito.verifyPrivate(memberService, times(1)).invoke("validateDuplicate", anyString(), anyString());
        verify(memberRepository, times(1)).existsByUsername(anyString());
        verify(memberRepository, times(1)).existsByNickname(anyString());
        verify(memberRepository, never()).save(any(Member.class));
        verify(emailSender, never()).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test
    public void 이메일인증() {
        //given
        MemberEmailAuthRequestDto dto = createEmailAuthRequestDto("user@mail.com");
        
        Member member = mock(Member.class);
        
        when(memberRepository.confirmEmailAuth(anyString(),
                                               anyString(),
                                               any(LocalDateTime.class))).thenReturn(Optional.of(member));
        
        //when
        memberService.emailAuthConfirm(dto);
        
        //then
        verify(memberRepository, times(1)).confirmEmailAuth(anyString(), anyString(), any(LocalDateTime.class));
        verify(member, times(1)).authenticateEmail();
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 이메일인증_계정엔티티조회불가() {
        //given
        MemberEmailAuthRequestDto dto = createEmailAuthRequestDto("user@mail.com");
        
        Member member = mock(Member.class);
        
        when(memberRepository.confirmEmailAuth(anyString(), anyString(), any(LocalDateTime.class))).thenThrow(
                EntityNotFoundException.class);
        
        //when
        memberService.emailAuthConfirm(dto);
        
        //then
        verify(memberRepository, times(1)).confirmEmailAuth(anyString(), anyString(), any(LocalDateTime.class));
        verify(member, never()).authenticateEmail();
    }
    
    @Test
    public void 인증메일재발송() {
        //given
        String username = "user@mail.com";
        
        Member    member    = mock(Member.class);
        EmailAuth emailAuth = mock(EmailAuth.class);
        
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(member.getUsername()).thenReturn(username);
        when(member.getEmailAuth()).thenReturn(emailAuth);
        when(emailAuth.getAuthToken()).thenReturn(UUID.randomUUID().toString());
        
        //when
        memberService.resendConfirmEmail(username);
        
        //then
        verify(memberRepository, times(1)).findByUsername(anyString());
        verify(member, times(1)).updateEmailAuthToken(anyString());
        verify(emailSender, times(1)).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 인증메일재발송_계정엔티티조회불가() {
        //given
        String username = "user@mail.com";
        
        Member member = mock(Member.class);
        
        when(memberRepository.findByUsername(anyString())).thenThrow(EntityNotFoundException.class);
        
        //when
        memberService.resendConfirmEmail(username);
        
        //then
        verify(memberRepository, times(1)).findByUsername(anyString());
        verify(member, never()).updateEmailAuthToken(anyString());
        verify(emailSender, never()).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test
    public void 비밀번호찾기() throws Exception {
        //given
        String username = "user@mail.com";
        
        Member    member    = mock(Member.class);
        EmailAuth emailAuth = mock(EmailAuth.class);
        
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(member.getEmailAuth()).thenReturn(emailAuth);
        when(emailAuth.getAuth()).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn(UUID.randomUUID().toString());
        when(member.getUsername()).thenReturn(username);
        when(member.getPassword()).thenReturn(UUID.randomUUID().toString());
        
        //when
        memberService.findPassword(username);
        
        //then
        verify(memberRepository, times(1)).findByUsername(anyString());
        verify(member, times(1)).getEmailAuth();
        verify(emailAuth, times(1)).getAuth();
        verify(memberService, never()).resendConfirmEmail(anyString());
        PowerMockito.verifyPrivate(memberService, times(1)).invoke("tempPasswordGenerator", anyInt());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(member, times(1)).updatePassword(anyString());
        verify(emailSender, times(1)).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 비밀번호찾기_계정엔티티조회불가() throws Exception {
        //given
        String username = "user@mail.com";
        
        Member    member    = mock(Member.class);
        EmailAuth emailAuth = mock(EmailAuth.class);
        
        when(memberRepository.findByUsername(anyString())).thenThrow(EntityNotFoundException.class);
        
        //when
        memberService.findPassword(username);
        
        //then
        verify(memberRepository, times(1)).findByUsername(anyString());
        verify(member, never()).getEmailAuth();
        verify(emailAuth, never()).getAuth();
        verify(memberService, never()).resendConfirmEmail(anyString());
        PowerMockito.verifyPrivate(memberService, never()).invoke("tempPasswordGenerator", anyInt());
        verify(passwordEncoder, never()).encode(anyString());
        verify(member, never()).updatePassword(anyString());
        verify(emailSender, never()).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test(expected = InvalidValueException.class)
    public void 비밀번호찾기_이메일인증미완료계정() throws Exception {
        //given
        String username = "user@mail.com";
        
        Member    member    = mock(Member.class);
        EmailAuth emailAuth = mock(EmailAuth.class);
        
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(member.getEmailAuth()).thenReturn(emailAuth);
        when(emailAuth.getAuth()).thenReturn(false);
        when(member.getUsername()).thenReturn(username);
        
        //when
        memberService.findPassword(username);
        
        //then
        verify(memberRepository, times(1)).findByUsername(anyString());
        verify(member, times(1)).getEmailAuth();
        verify(emailAuth, times(1)).getAuth();
        verify(memberService, times(1)).resendConfirmEmail(anyString());
        PowerMockito.verifyPrivate(memberService, never()).invoke("tempPasswordGenerator", anyInt());
        verify(passwordEncoder, never()).encode(anyString());
        verify(member, never()).updatePassword(anyString());
        verify(emailSender, never()).send(any(EmailSender.SendType.class), anyString(), anyString());
    }
    
    @Test
    public void 아이디찾기() {
        //given
        String username = "user@mail.com";
        
        Member       member       = mock(Member.class);
        ProfileImage profileImage = mock(ProfileImage.class);
        ArrayList    arrayList    = mock(ArrayList.class);
        
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(member.getProfileImage()).thenReturn(profileImage);
        when(member.getRole()).thenReturn(Role.USER);
        when(member.getPosts()).thenReturn(arrayList);
        when(member.getComments()).thenReturn(arrayList);
        
        //when
        memberService.findByUsername(username);
        
        //then
        verify(memberRepository, times(1)).findByUsername(anyString());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 아이디찾기_계정엔티티조회불가() {
        //given
        String username = "user@mail.com";
        
        Member member = mock(Member.class);
        
        when(memberRepository.findByUsername(anyString())).thenThrow(EntityNotFoundException.class);
        
        //when
        memberService.findByUsername(username);
        
        //then
        verify(memberRepository, never()).findByUsername(anyString());
    }
    
    @Test
    public void 회원정보수정_닉네임_서명() throws Exception {
        //given
        MultipartFile          file = mock(MultipartFile.class);
        MemberUpdateRequestDto dto  = createUpdateRequestDto(0L, "User12!@", "사용자", "사용자 서명입니다.", null, file);
        
        Member         member       = mock(Member.class);
        EmailAuth      emailAuth    = mock(EmailAuth.class);
        Base64.Encoder encoder      = mock(Base64.Encoder.class);
        FileStore      fileStore    = mock(FileStore.class);
        ProfileImage   profileImage = mock(ProfileImage.class);
        
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(member.getPassword()).thenReturn(UUID.randomUUID().toString());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(member.getEmailAuth()).thenReturn(emailAuth);
        when(emailAuth.getAuth()).thenReturn(true);
        when(member.getNickname()).thenReturn("닉네임");
        when(file.isEmpty()).thenReturn(true);
        
        //when
        memberService.update(dto, false);
        
        //then
        verify(memberRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(member, times(1)).getEmailAuth();
        verify(emailAuth, times(1)).getAuth();
        verify(memberService, never()).resendConfirmEmail(anyString());
        PowerMockito.verifyPrivate(memberService, never()).invoke("validateDuplicate", anyString(), anyString());
        verify(member, times(1)).updateNickname(anyString());
        verify(member, times(1)).updateSignature(anyString());
        verify(file, times(1)).isEmpty();
        verify(encoder, never()).encodeToString(any(byte[].class));
        verify(fileStore, never()).storeFile(any(MultipartFile.class));
        verify(member, never()).getProfileImage();
        verify(profileImage, never()).getStoreFilePath();
        verify(profileImage, never()).updateImage(anyString(), anyString(), anyString(), anyLong(), anyString());
        verify(fileStore, never()).deleteUploadedFile(anyString());
        verify(profileImage, never()).delete();
        verify(member, never()).updatePassword(anyString());
    }
    
    private MemberJoinRequestDto createJoinRequestDto(String username, String password, String nickname) {
        MemberJoinRequestDto dto = new MemberJoinRequestDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setNickname(nickname);
        return dto;
    }
    
    private MemberEmailAuthRequestDto createEmailAuthRequestDto(String email) {
        MemberEmailAuthRequestDto dto = new MemberEmailAuthRequestDto();
        dto.setEmail(email);
        dto.setAuthToken(UUID.randomUUID().toString());
        dto.setCurrentTime(LocalDateTime.now());
        return dto;
    }
    
    private MemberUpdateRequestDto createUpdateRequestDto(Long id,
                                                          String password,
                                                          String nickname,
                                                          String signature,
                                                          String newPassword,
                                                          MultipartFile file) {
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto();
        dto.setId(id);
        dto.setPassword(password);
        dto.setNickname(nickname);
        dto.setSignature(signature);
        dto.setNewPassword(newPassword);
        dto.setProfileImage(file);
        return dto;
    }
    
    private int createRandomPositiveInt() {
        Random random = new Random();
        int    num    = 0;
        while (num < 0) num = random.nextInt();
        return num;
    }
    
    private long createRandomPositiveLong() {
        Random random = new Random();
        long   num    = 0;
        while (num < 0L) num = random.nextLong();
        return num;
    }
    
}
