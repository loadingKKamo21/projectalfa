# Project alfa

## 개요
Spring Boot 기반 간단한 CRUD 프로젝트

## 목차
1. [개발 환경](#개발-환경)
2. [설계 목표](#설계-목표)
3. [프로젝트 정보](#프로젝트-정보)
    - [Tree](#-tree-)
    - [Backend](#-backend-)
    - [Frontend](#-frontend-)
4. [프로젝트 구현 화면](#프로젝트-구현-화면)
5. [프로젝트 실행](#프로젝트-실행)

## 개발 환경
#### Backend
- Java, Spring Framework (Cache, Data JPA, OAuth2, Security, Web), Querydsl
- JUnit 4, Mockito 3, PowerMock
#### Frontend
- JavaScript, jQuery, Thymeleaf, Bootstrap, [Summernote API](https://summernote.org/)
- Bootstrap Theme : [Start Bootstrap - SB Admin 2](https://github.com/StartBootstrap/startbootstrap-sb-admin-2)
#### DB
- H2 Database, Redis
- Embedded Redis (for Test)
#### Tool
- IntelliJ IDEA, Gradle, SmartGit

## 설계 목표
- 스프링 부트를 활용한 CRUD 웹 애플리케이션
- JPA 활용 설계
- 게시글 및 댓글 페이징, 게시글 검색 기능
- 스프링 시큐리티, 이메일 인증을 활용한 계정 인증
- 파일 업로드 기능
- 스프링 캐시 + Redis 

## 프로젝트 정보
#### [ Tree ]
```
\---src
    +---main
    |   +---java
    |   |   \---com
    |   |       \---project
    |   |           \---alfa
    |   |               |   AlfaApplication.java
    |   |               |   InitDb.java                                                    -> 더미 데이터 추가
    |   |               |   
    |   |               +---common
    |   |               |   +---auth                                                       ┌────────── 스프링 시큐리티 ───────────┐
    |   |               |   |   |   CustomAuthenticationFailureHandler.java
    |   |               |   |   |   CustomAuthenticationProvider.java
    |   |               |   |   |   CustomUserDetails.java                                 -> UserDetails, OAuth2User 구현체
    |   |               |   |   |   CustomUserDetailsService.java
    |   |               |   |   |   WebSecurityConfig.java                                 -> 웹 시큐리티 설정
    |   |               |   |   |   
    |   |               |   |   \---oauth
    |   |               |   |       |   CustomOAuth2UserService.java                       -> OAuth2 인증 관련 서비스
    |   |               |   |       |   
    |   |               |   |       \---provider
    |   |               |   |               GoogleUserInfo.java                            -> 구글 계정용 OAuth2UserInfo 구현체
    |   |               |   |               OAuth2UserInfo.java                            -> OAuth2 데이터용 인터페이스
    |   |               |   |                                                              └────────────────────────────────────┘
    |   |               |   +---config                                                     ┌────────────── 빈 등록 ──────────────┐
    |   |               |   |       CacheConfig.java                                       -> 캐시 매니저 등록, 커스텀 키 생성
    |   |               |   |       QuerydslConfig.java
    |   |               |   |       RedisConfig.java                                       -> Redis 연동 설정
    |   |               |   |       SecurityConfig.java
    |   |               |   |       WebConfig.java                                         -> 인터셉터 등록
    |   |               |   |                                                              └────────────────────────────────────┘
    |   |               |   +---error                                                      ┌─────────── 스프링 예외 전략 ──────────┐
    |   |               |   |   |   ErrorResponse.java
    |   |               |   |   |   GlobalExceptionHandler.java                            * Spring Exception Guide 참고:
    |   |               |   |   |                                                            https://cheese10yun.github.io/spring-guide-exception/
    |   |               |   |   \---exception
    |   |               |   |           BusinessException.java
    |   |               |   |           EntityNotFoundException.java
    |   |               |   |           ErrorCode.java
    |   |               |   |           InvalidValueException.java
    |   |               |   |                                                              └────────────────────────────────────┘
    |   |               |   +---interceptor
    |   |               |   |       LogInterceptor.java                                    -> 로깅 인터셉터
    |   |               |   |       
    |   |               |   \---util
    |   |               |           EmailSender.java                                       -> 인증 메일 전송
    |   |               |           FileStore.java                                         -> 파일 저장 및 기록
    |   |               |           
    |   |               +---controller                                                     ┌────────────── 컨트롤러 ──────────────┐
    |   |               |       CommentController.java
    |   |               |       HomeController.java                                        -> 로그인, 회원가입 등 권한 없는 접근
    |   |               |       MemberController.java
    |   |               |       PostController.java
    |   |               |                                                                  └────────────────────────────────────┘
    |   |               +---domain                                                         ┌─────────────── 엔티티 ──────────────┐
    |   |               |       BaseTimeEntity.java                                        -> 생성일시/최종 수정일시 기록
    |   |               |       Category.java                                              -> 카테고리
    |   |               |       Comment.java                                               -> 댓글
    |   |               |       EmailAuth.java                                             -> 이메일 인증 정보(Member embeded)
    |   |               |       Member.java                                                -> 회원
    |   |               |       PersistentLogins.java                                      -> 로그인 유지(Remember-me)
    |   |               |       Post.java                                                  -> 게시글
    |   |               |       ProfileImage.java                                          -> 프로필 사진, UploadFile 구현체
    |   |               |       Role.java                                                  -> 계정 유형(Enum)
    |   |               |       UploadFile.java                                            -> 업로드 파일 추상 클래스
    |   |               |                                                                  └────────────────────────────────────┘
    |   |               +---repository                                                     ┌─────────────── DAO ────────────────┐
    |   |               |   |   CategoryRepository.java
    |   |               |   |   CommentRepository.java
    |   |               |   |   MemberRepository.java
    |   |               |   |   PostRepository.java
    |   |               |   |   
    |   |               |   \---querydsl                                                   -> Querydsl 적용 인터페이스 및 구현체
    |   |               |           CommentRepositoryCustom.java
    |   |               |           CommentRepositoryImpl.java
    |   |               |           PostRepositoryCustom.java
    |   |               |           PostRepositoryImpl.java
    |   |               |                                                                  └────────────────────────────────────┘
    |   |               \---service                                                        ┌─────────────── 서비스 ──────────────┐
    |   |                   |   CategoryService.java
    |   |                   |   CommentService.java
    |   |                   |   MemberService.java
    |   |                   |   PostService.java
    |   |                   |                                                              └────────────────────────────────────┘
    |   |                   \---dto                                                        ┌─────────────── DTO ────────────────┐
    |   |                           CategoryRequestDto.java
    |   |                           CategoryResponseDto.java
    |   |                           CommentRequestDto.java
    |   |                           CommentResponseDto.java
    |   |                           MemberEmailAuthRequestDto.java
    |   |                           MemberJoinRequestDto.java
    |   |                           MemberResponseDto.java
    |   |                           MemberUpdateRequestDto.java
    |   |                           PostListResponseDto.java
    |   |                           PostReadResponseDto.java
    |   |                           PostWriteRequestDto.java
    |   |                                                                                  └────────────────────────────────────┘
    |   \---resources
    |       |   application.yml                                                            -> 애플리케이션 설정, Test용 프로필 분리
    |       |   
    |       +---static
    |       |   |   
    |       |   +---css
    |       |   |       
    |       |   +---img
    |       |   |       
    |       |   +---js
    |       |   |           
    |       |   \---vendor
    |       |               
    |       \---templates
    |           |   forgot-password.html
    |           |   index.html
    |           |   layout.html
    |           |   login.html
    |           |   register.html
    |           |   
    |           +---error
    |           |       
    |           +---fragments                                                              -> 타임리프 템플릿
    |           |       body.html
    |           |       head.html
    |           |       
    |           +---layout                                                                 -> 타임리프 레이아웃
    |           |       basic.html
    |           |       layout.html
    |           |       
    |           +---members
    |           |       activity.html
    |           |       profile-update.html
    |           |       profile.html
    |           |       
    |           +---posts
    |           |       list.html
    |           |       notice.html
    |           |       read.html
    |           |       update.html
    |           |       write.html
    |           |       
    |           \---templates
    |                       
    \---test
        \---java
            \---com
                \---project
                    \---alfa
                        |   AlfaApplicationTests.java
                        |   
                        +---config
                        |       EmbeddedRedisConfig.java                                   -> 테스트용 Embedded Redis 설정
                        |       
                        +---repository                                                     ┌───────────── DAO 테스트 ─────────────┐
                        |       CategoryRepositoryTest.java
                        |       CommentRepositoryTest.java
                        |       MemberRepositoryTest.java
                        |       PostRepositoryTest.java
                        |                                                                  └────────────────────────────────────┘
                        \---service                                                        ┌──────────── 서비스 테스트 ────────────┐
                                MemberServiceTest.java
                                MemberServiceTestWithMockito.java                          -> Mockito 적용 
                                PostServiceTest.java
                                                                                           └────────────────────────────────────┘
```
#### [ Backend ]
- 게시글 및 댓글 Create / Read / Update / Delete
- 게시글 및 댓글 목록 페이징 및 게시글 검색 기능
- 스프링 시큐리티 연동 회원(계정) 기능, 가입 방식 2가지 적용
  - 아이디/비밀번호 + 이메일 인증
  - 구글 계정 가입(OAuth2)
- 스프링 데이터 JPA 및 Querydsl 활용 엔티티 및 리포지토리 설계
- OSIV OFF 기본 설정
  - 서비스 레이어 외부로 엔티티 노출 억제
  - 컨트롤러 - 서비스 전송 간 DTO만 사용
![OSIV](https://user-images.githubusercontent.com/113382975/233977893-74df076b-9383-41ad-8df7-43b27792c29b.jpg)
- Redis 캐시 활용 : 게시글 목록 조회, 게시글 조회
  - 조회 수 증가 로직 연동
    - ~~쿠키 활용~~
    - Redis 캐시 활용
- 게시글 = Model 활용 / 댓글 = Ajax 활용
- Mockito 사용 테스트 시 private method 테스트를 위한 PowerMock 사용(JUnit 4)
- 테스트 시 Redis 설치 유무와 관계 없는 진행을 위해 Embedded Redis 추가
#### [ Frontend ]
- 부트스트랩 테마 기반 타임리프 적용 페이지 구현
    - 타임리프 템플릿 및 레이아웃([Thymeleaf Layout Dialect](https://github.com/ultraq/thymeleaf-layout-dialect)) 사용
- 게시글 조회 페이지의 댓글, 회원 정보 페이지 작성 게시글 / 댓글 렌더링 시 Ajax, 자바스크립트 활용

## 프로젝트 구현 화면
- 아이디 / 비밀번호 로그인 → 로그아웃
    ![login_logout](https://user-images.githubusercontent.com/113382975/234009591-6e5ebb4f-be4e-4c94-8030-2691b13b930f.gif)
- OAuth2 구글 로그인
    ![oauth_login](https://user-images.githubusercontent.com/113382975/234010483-5e9d203d-53af-421f-b82c-1df25e57f395.gif)
- 아이디 / 비밀번호 계정 프로필 → 수정
    ![profile](https://user-images.githubusercontent.com/113382975/234011164-a5502c25-7d4f-4fb4-ada2-97929afc1761.gif)
- OAuth2 구글 계정 프로필 → 수정
    ![oauth_profile](https://user-images.githubusercontent.com/113382975/234011218-27da77b4-ad7f-43b9-8861-b35567ca6a38.gif)
- 나의 활동 → 작성 게시글 / 작성 댓글
    ![activity](https://user-images.githubusercontent.com/113382975/234011274-116eaeeb-b010-48f1-935d-ca522a006285.gif)
- 게시판 탐색
    ![board](https://user-images.githubusercontent.com/113382975/234010798-8dbe3309-d8d3-4c12-bd31-8739b77c2b83.gif)
- 공지글 상단 고정
    ![notice](https://user-images.githubusercontent.com/113382975/234011557-607b6879-3e31-4b16-b85c-f8d7e31bb541.gif)
- 게시글 작성(Summernote)
    ![create](https://user-images.githubusercontent.com/113382975/234012330-c0f4a0cc-6abd-4a28-996c-416a0608c5a4.jpg)
- 게시글 조회(Summernote)
    ![read](https://user-images.githubusercontent.com/113382975/234012442-0bf2a0cd-ad14-479b-8098-e487680590de.jpg)
- 댓글 페이징, 수정
    ![comment](https://user-images.githubusercontent.com/113382975/234011923-ab2abe73-35fd-4dc5-81f9-3ae08065f4da.gif)

## 프로젝트 실행
- 기본 설정값 기반, 조건에 따라 설정값 변경 필요
- application.yml 설정
    - DB : [H2 Database](https://www.h2database.com/html/main.html) 와 [Redis](https://redis.io/) 설치 / 실행
    ```
    ...
    spring:
        datasource:
            driver-class-name: org.h2.Driver
            url: jdbc:h2:tcp://localhost/~/test
            username: sa
            password:
        ...
        redis:
            host: localhost
            port: 6379
            password:
            lettuce:
                pool:
                    min-idle: 0
                    max-idle: 8
                    max-active: 8
    ...
    ```
    - SMTP : 이메일 인증(회원가입, 비밀번호 찾기) 시 사용, 기본값으로 Google SMTP 사용
    ```
    ...
    spring:
        mail:
            host: smtp.gmail.com
            port: 587
            username: {Google Username}
            password: {Google Password}
            properties:
                ...
    ...
    ```
    - OAuth2 : OAuth2 인증 시 사용, 기본값으로 Google 사용 -> 타 OAuth2 사용 시 OAuth2UserInfo 구현체 생성 필요
    ```
    ...
    spring:
        security:
            oauth2:
                client:
                    registration:
                        google:
                            client-id: {Google OAuth 2.0 Client-id}
                            client-secret: {Google OAuth 2.0 Client-Secret}
                            scope:
                                - email
                                - profile
    ...
    ```
    - File Upload Path : 파일 업로드(프로필 사진 등록) 시 사용
    ```
    ...
    file:
        upload:
            location: {Upload Path}
    ...
    ```
- 더미 데이터 추가 : InitDb.class, @Component 활성화 및 spring.jpa.hibernate.ddl-auto 설정
    - 계정, 게시글, 댓글 추가
    ```java
    //@Component
    @RequiredArgsConstructor
    public class InitDb {
        ...
    }
    ```
    ```
    ...
    spring:
        jpa:
            hibernate:
                ddl-auto: create
            ...
    ...
    ```
- 엔티티 Querydsl QClass 생성 필요
