package com.project.alfa;

import com.project.alfa.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

//@Component
@RequiredArgsConstructor
public class InitDb {
    
    @PersistenceContext
    private final EntityManager   em;
    private final PasswordEncoder passwordEncoder;
    
    private char[] caseSet = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        List<Member> members = new ArrayList<>();
        List<Post>   posts   = new ArrayList<>();
        Member       member  = null;
        Post         post    = null;
        Random       random  = new Random();
        
        Category all = Category.builder().name("ALL").build();
        em.persist(all);
        
        Category free = Category.builder().name("FREE").build();
        em.persist(free);
        free.setParent(all);
        all.addChildCategory(free);
        
        Category qna = Category.builder().name("QNA").build();
        em.persist(qna);
        qna.setParent(all);
        all.addChildCategory(qna);
        
        for (char c : caseSet) {
            EmailAuth emailAuth = EmailAuth.builder().authToken(UUID.randomUUID().toString()).build();
            emailAuth.useToken();
            ProfileImage profileImage = ProfileImage.builder().build();
            em.persist(profileImage);
            member = Member.builder()
                           .username("admin" + c + "@mail.com")
                           .password(passwordEncoder.encode("Admin12!@"))
                           .emailAuth(emailAuth)
                           .nickname("관리자" + c)
                           .profileImage(profileImage)
                           .build();
            member.updateSignature("관리자" + c + " 입니다.");
            
            if (member.getRole() != Role.ADMIN) member.changeRole();
            
            em.persist(member);
            members.add(member);
            
            if (member.getId() % 5 == 0) {
                post = Post.builder()
                           .writer(member)
                           .title("전체 게시판 공지글" + c)
                           .content("전체 게시판 공지글" + c + " 입니다.")
                           .category(all)
                           .noticeYn(true)
                           .build();
                em.persist(post);
                for (int i = 0; i < new Random().nextInt(1000); i++)
                    post.addViewCount();
                posts.add(post);
            }
            
            if (member.getId() % 10 == 0) {
                post = Post.builder()
                           .writer(member)
                           .title("자유 게시판 공지글" + c)
                           .content("자유 게시판 공지글" + c + " 입니다.")
                           .category(free)
                           .noticeYn(true)
                           .build();
                em.persist(post);
                for (int i = 0; i < new Random().nextInt(1000); i++)
                    post.addViewCount();
                posts.add(post);
                
                post = Post.builder()
                           .writer(member)
                           .title("질문 게시판 공지글" + c)
                           .content("질문 게시판 공지글" + c + " 입니다.")
                           .category(qna)
                           .noticeYn(true)
                           .build();
                em.persist(post);
                post.addViewCount();
                for (int i = 0; i < new Random().nextInt(1000); i++)
                    post.addViewCount();
                posts.add(post);
            }
        }
        
        for (char c : caseSet) {
            EmailAuth emailAuth = EmailAuth.builder().authToken(UUID.randomUUID().toString()).build();
            emailAuth.useToken();
            ProfileImage profileImage = ProfileImage.builder().build();
            em.persist(profileImage);
            member = Member.builder()
                           .username("user" + c + "@mail.com")
                           .password(passwordEncoder.encode("User12!@"))
                           .emailAuth(emailAuth)
                           .nickname("사용자" + c)
                           .profileImage(profileImage)
                           .build();
            member.updateSignature("사용자" + c + " 입니다.");
            
            em.persist(member);
            members.add(member);
            
            for (char p : caseSet) {
                post = Post.builder()
                           .writer(member)
                           .title("자유 게시판 게시글" + p)
                           .content("자유 게시판 게시글" + p + " 입니다.")
                           .category(free)
                           .noticeYn(false)
                           .build();
                em.persist(post);
                for (int i = 0; i < new Random().nextInt(1000); i++)
                    post.addViewCount();
                posts.add(post);
                
                post = Post.builder()
                           .writer(member)
                           .title("질문 게시판 게시글" + p)
                           .content("질문 게시판 게시글" + p + " 입니다.")
                           .category(qna)
                           .noticeYn(false)
                           .build();
                em.persist(post);
                post.addViewCount();
                for (int i = 0; i < new Random().nextInt(1000); i++)
                    post.addViewCount();
                posts.add(post);
            }
        }
        
        for (Post p : posts) {
            int i = random.nextInt(members.size());
            while (i > 0) {
                member = members.get(random.nextInt(members.size()));
                em.persist(Comment.builder().writer(member).post(p).content("댓글 입니다.").build());
                i--;
            }
        }
    }
    
}
