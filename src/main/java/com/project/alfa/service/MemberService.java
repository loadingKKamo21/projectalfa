package com.project.alfa.service;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.ErrorCode;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.common.util.EmailSender;
import com.project.alfa.common.util.FileStore;
import com.project.alfa.domain.*;
import com.project.alfa.repository.CommentRepository;
import com.project.alfa.repository.MemberRepository;
import com.project.alfa.repository.PostRepository;
import com.project.alfa.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository  memberRepository;
    private final EmailSender       emailSender;
    private final PasswordEncoder   passwordEncoder;
    private final FileStore         fileStore;
    private final PostRepository    postRepository;
    private final CommentRepository commentRepository;
    
    /**
     * 회원가입
     *
     * @param dto - 회원가입 정보
     * @return 회원 PK
     */
    @Transactional
    public Long join(final MemberJoinRequestDto dto) {
        //아이디, 이메일, 닉네임 중복 검증
        validateDuplicate(dto.getUsername(), dto.getNickname());
        
        Member member = Member.builder()
                              .username(dto.getUsername())
                              .password(passwordEncoder.encode(dto.getPassword())) //비밀번호 암호화
                              .emailAuth(EmailAuth.builder().authToken(UUID.randomUUID().toString()).build())
                              .nickname(dto.getNickname())
                              .profileImage(ProfileImage.builder()
                                                        .originalFileName(null)
                                                        .storeFileName(null)
                                                        .storeFilePath(null)
                                                        .fileSize(null)
                                                        .base64Data(null)
                                                        .build())
                              .build();
        
        memberRepository.save(member);
        
        //회원가입 인증 메일 발송
        emailSender.send(EmailSender.SendType.AUTH, member.getUsername(), member.getEmailAuth().getAuthToken());
        
        return member.getId();
    }
    
    /**
     * 이메일 인증
     *
     * @param dto - 이메일 인증 정보
     */
    @Transactional
    public void emailAuthConfirm(final MemberEmailAuthRequestDto dto) {
        memberRepository.confirmEmailAuth(dto.getEmail(), dto.getAuthToken(), dto.getCurrentTime())
                        .orElseThrow(() -> new EntityNotFoundException("Could not found 'Member' entity by email: " + dto.getEmail() + ", authToken: " + dto.getAuthToken() + ", later time than currentTime: " + dto.getCurrentTime()))
                        .authenticateEmail();
    }
    
    /**
     * 인증 메일 재발송
     *
     * @param username - 이메일 주소
     */
    @Transactional
    public void resendConfirmEmail(final String username) {
        Member member = memberRepository.findByUsername(username)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' entity by username: " + username));
        
        member.updateEmailAuthToken(UUID.randomUUID().toString());
        
        emailSender.send(EmailSender.SendType.AUTH, member.getUsername(), member.getEmailAuth().getAuthToken());
    }
    
    /**
     * 비밀번호 찾기 결과 메일 발송
     *
     * @param username - 아이디
     */
    @Transactional
    public void findPassword(final String username) {
        Member member = memberRepository.findByUsername(username)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' entity by username: " + username));
        
        if (!member.getEmailAuth().getAuth()) {
            resendConfirmEmail(member.getUsername());
            throw new InvalidValueException("Email auth not completed", ErrorCode.EMAIL_AUTH_NOT_COMPLETED);
        }
        
        //20자리 임시 비밀번호 생성
        String tempPassword = tempPasswordGenerator(20);
        member.updatePassword(passwordEncoder.encode(tempPassword)); //임시 비밀번호로 변경
        
        //비밀번호 찾기 결과 메일 발송
        emailSender.send(EmailSender.SendType.FIND_PASSWORD, member.getUsername(), member.getPassword());
    }
    
    /**
     * 회원정보 조회
     *
     * @param username - 아이디
     * @return 회원 상세정보
     */
    public MemberResponseDto findByUsername(final String username) {
        Member member = memberRepository.findByUsername(username)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' entity by username: " + username));
        List<Post>    recent10Posts    = postRepository.findTopNByPeriod(member, 10, null);
        List<Comment> recent10Comments = commentRepository.findTopNByPeriod(member, 10, null);
        
        return new MemberResponseDto(member, recent10Posts, recent10Comments);
    }
    
    /**
     * 회원정보 수정
     *
     * @param dto         - 회원정보
     * @param deleteImage - 프로필 사진 삭제 여부
     */
    @Transactional
    public void update(final MemberUpdateRequestDto dto, final boolean deleteImage) {
        Member member = memberRepository.findById(dto.getId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' entity by id: " + dto.getId()));
        
        //비밀번호 확인
        if (member.getProvider() == null && member.getProviderId() == null)
            if (!passwordEncoder.matches(dto.getPassword(), member.getPassword()))
                throw new InvalidValueException("Invalid input value", ErrorCode.PASSWORD_DO_NOT_MATCH);
        
        if (!member.getEmailAuth().getAuth()) {
            resendConfirmEmail(member.getUsername());
            throw new InvalidValueException("Email auth not completed", ErrorCode.EMAIL_AUTH_NOT_COMPLETED);
        }
        
        //닉네임 중복 검증
        if (!member.getNickname().equals(dto.getNickname())) validateDuplicate(null, dto.getNickname());
        
        //닉네임 변경
        member.updateNickname(dto.getNickname());
        
        //서명 변경
        member.updateSignature(dto.getSignature());
        
        //프로필 사진 변경
        MultipartFile file = dto.getProfileImage();
        if (!file.isEmpty()) {
            UploadFile storeFile;
            String     base64Data;
            try {
                base64Data = Base64.getEncoder().encodeToString(file.getBytes());
                storeFile = fileStore.storeFile(file);
                
                if (member.getProfileImage().getStoreFilePath() == null) //프로필 사진 최초 등록 시
                    member.getProfileImage()
                          .updateImage(storeFile.getOriginalFileName(),
                                       storeFile.getStoreFileName(),
                                       storeFile.getStoreFilePath(),
                                       file.getSize(),
                                       base64Data);
                else {    //프로필 사진 변경 시
                    ProfileImage profileImage = member.getProfileImage();
                    fileStore.deleteUploadedFile(profileImage.getStoreFilePath());
                    
                    profileImage.updateImage(storeFile.getOriginalFileName(),
                                             storeFile.getStoreFileName(),
                                             storeFile.getStoreFilePath(),
                                             file.getSize(),
                                             base64Data);
                }
            } catch (IOException e) {
                throw new InvalidValueException(e.getMessage());
            }
        }
        
        //프로필 사진 삭제
        if (deleteImage) {
            ProfileImage profileImage = member.getProfileImage();
            fileStore.deleteUploadedFile(profileImage.getStoreFilePath());
            profileImage.delete();
        }
        
        //비밀번호 변경
        if ((dto.getNewPassword() != null && !dto.getNewPassword()
                                                 .isEmpty()) && !passwordEncoder.matches(dto.getNewPassword(),
                                                                                         member.getPassword()))
            member.updatePassword(passwordEncoder.encode(dto.getNewPassword()));
    }
    
    /**
     * 회원 탈퇴
     *
     * @param username - 아이디
     * @param password - 비밀번호 확인
     */
    @Transactional
    public void delete(final String username, final String password) {
        Member member = memberRepository.findByUsername(username)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' entity by username: " + username));
        
        if (member.getProvider() == null && member.getProviderId() == null)
            if (!passwordEncoder.matches(password, member.getPassword()))
                throw new InvalidValueException("Invalid input value", ErrorCode.PASSWORD_DO_NOT_MATCH);
        
        //프로필 사진 파일 삭제
        if (member.getProfileImage().getStoreFilePath() != null)
            fileStore.deleteUploadedFile(member.getProfileImage().getStoreFilePath());
        memberRepository.delete(member);
    }
    
    /**
     * 나의 활동 조회
     *
     * @param username - 아이디
     * @param pageable - 페이징 객체
     * @param clazz    - 조회할 DTO 클래스
     *                 1. 게시글 -> PostListResponseDto.class
     *                 2. 댓글 -> CommentResponseDto.class
     * @param <T>
     * @return 조회된 게시글 or 댓글 목록
     */
    public <T> Page<T> myActivityContents(final String username, Pageable pageable, Class<T> clazz) {
        Member member = memberRepository.findByUsername(username)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                "Could not found 'Member' entity by username: " + username));
        
        if (clazz.equals(PostListResponseDto.class))
            return (Page<T>) postRepository.findByPeriod(member, null, pageable).map(PostListResponseDto::new);
        else if (clazz.equals(CommentResponseDto.class))
            return (Page<T>) commentRepository.findByPeriod(member, null, pageable).map(CommentResponseDto::new);
        else throw new InvalidValueException("Invalid type value: " + clazz.getName(), ErrorCode.INVALID_TYPE_VALUE);
    }
    
    
    //==================== 검증 메서드 ====================//
    
    /**
     * 회원가입 시 아이디, 닉네임 중복 검증
     *
     * @param username - 아이디
     * @param nickname - 닉네임
     */
    private void validateDuplicate(final String username, final String nickname) {
        //아이디 중복 확인
        if ((username != null && !username.trim().isEmpty()) && memberRepository.existsByUsername(username))
            throw new InvalidValueException("Invalid input value: " + username, ErrorCode.USERNAME_DUPLICATION);
        //닉네임 중복 확인
        if ((nickname != null && !nickname.trim().isEmpty()) && memberRepository.existsByNickname(nickname))
            throw new InvalidValueException("Invalid input value: " + nickname, ErrorCode.NICKNAME_DUPLICATION);
    }
    
    //==================== 임시 비밀번호 생성 메서드 ====================//
    
    //임시 비밀번호 생성용 숫자
    private final char[] numSet       = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    //임시 비밀번호 생성용 영문 소문자
    private final char[] lowerCaseSet = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    //임시 비밀번호 생성용 영문 대문자
    private final char[] upperCaseSet = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    //임시 비밀번호 생성용 특수문자
    private final char[] specialSet   = { '`', '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=',
            '+', '[', '{', ']', '}', '\\', '|', ';', ':', '\'', '"', ',', '<', '.', '>', '/', '?' };
    
    /**
     * 임시 비밀번호 생성
     *
     * @param size - 비밀번호 글자 수
     * @return 임시 비밀번호
     */
    private String tempPasswordGenerator(final int size) {
        StringBuilder sb   = new StringBuilder();
        Random        rand = new Random();
        
        int n = 0, l = 0, u = 0, s = 0;
        
        //숫자, 영문 대/소문자, 특수문자 각 최소 1개씩 포함
        while (!(n + l + u + s == size)) {
            n = rand.nextInt(size) + 1;
            l = rand.nextInt(size) + 1;
            u = rand.nextInt(size) + 1;
            s = rand.nextInt(size) + 1;
        }
        
        for (int i = 0; i < n; i++) sb.append(numSet[rand.nextInt(numSet.length - 1)]);
        for (int i = 0; i < l; i++) sb.append(lowerCaseSet[rand.nextInt(lowerCaseSet.length - 1)]);
        for (int i = 0; i < u; i++) sb.append(upperCaseSet[rand.nextInt(upperCaseSet.length - 1)]);
        for (int i = 0; i < s; i++) sb.append(specialSet[rand.nextInt(specialSet.length - 1)]);
        
        String       rawPassword    = sb.toString();
        List<String> generatedChars = Arrays.asList(rawPassword.split(""));
        
        Collections.shuffle(generatedChars);
        
        String shufflePassword = "";
        for (String string : generatedChars) shufflePassword += string;
        
        return shufflePassword;
    }
    
}
