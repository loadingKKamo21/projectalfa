package com.project.alfa.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "tb_members")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(exclude = { "posts", "comments" }, callSuper = false)
public class Member extends BaseTimeEntity {
    
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;                                    //PK
    
    @Column(nullable = false, updatable = false, unique = true)
    @Size(min = 5)
    private String username;                            //아이디
    
    @Column(nullable = false)
    private String password;                            //비밀번호
    
    @Embedded
    private EmailAuth emailAuth;                        //이메일 인증 정보
    
    @Column(nullable = false, unique = true)
    @Size(min = 1, max = 20)
    private String nickname;                            //닉네임
    
    @Size(max = 100)
    private String signature;                           //서명
    
    @OneToOne(cascade = PERSIST, orphanRemoval = true)
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;                  //프로필 이미지
    
    @Enumerated(STRING)
    @Column(nullable = false)
    private Role role;                                  //계정 권한
    
    private String provider;                            //OAuth 2.0 Provider
    private String providerId;                          //OAuth 2.0 ProviderId
    
    @OneToMany(mappedBy = "writer", orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();       //작성한 게시글 리스트
    
    @OneToMany(mappedBy = "writer", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>(); //작성한 댓글 리스트
    
    @Builder
    public Member(String username, String password, EmailAuth emailAuth, String nickname, ProfileImage profileImage) {
        this.username = username.toLowerCase();
        this.password = password;
        this.emailAuth = emailAuth;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = Role.USER;
    }
    
    //==================== 계정 정보 수정 메서드 ====================//
    
    /**
     * 비밀번호 변경
     *
     * @param newPassword - 수정할 비밀번호
     */
    public void updatePassword(String newPassword) {
        if ((newPassword != null && !newPassword.trim().isEmpty()) && !password.equals(newPassword))
            password = newPassword;
    }
    
    /**
     * 이메일 인증
     */
    public void authenticateEmail() {
        emailAuth.useToken();
    }
    
    /**
     * 이메일 인증 토큰 업데이트
     *
     * @param newAuthToken - 새로운 인증 토큰
     */
    public void updateEmailAuthToken(String newAuthToken) {
        if ((newAuthToken != null && !newAuthToken.trim().isEmpty()) && !emailAuth.getAuthToken().equals(newAuthToken))
            emailAuth.updateToken(newAuthToken);
    }
    
    /**
     * 닉네임 변경
     *
     * @param newNickname - 수정할 닉네임
     */
    public void updateNickname(String newNickname) {
        if ((newNickname != null && !newNickname.trim().isEmpty()) && !nickname.equals(newNickname))
            nickname = newNickname;
    }
    
    /**
     * 서명 변경
     *
     * @param newSignature - 수정할 서명
     */
    public void updateSignature(String newSignature) {
        if (signature == null) signature = newSignature;
        else if (!signature.equals(newSignature)) signature = newSignature;
    }
    
    /**
     * 프로필 이미지 변경
     *
     * @param newProfileImage - 수정할 프로필 이미지
     */
    public void updateProfileImage(ProfileImage newProfileImage) {
        profileImage = newProfileImage;
    }
    
    /**
     * 계정 유형 변경
     */
    public void changeRole() {
        if (role == Role.USER) role = Role.ADMIN;
        else role = Role.USER;
    }
    
    //==================== OAuth 2.0 계정 설정 메서드 ====================//
    
    /**
     * OAuth 2.0 가입 계정 추가 정보 입력
     *
     * @param provider   - OAuth 2.0 Provider
     * @param providerId - OAuth 2.0 ProviderId
     */
    public void setOAuthInfo(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }
    
}
