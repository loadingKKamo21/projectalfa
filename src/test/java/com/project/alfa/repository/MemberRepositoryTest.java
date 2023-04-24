package com.project.alfa.repository;

import com.project.alfa.domain.EmailAuth;
import com.project.alfa.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MemberRepositoryTest {
    
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
    public void 저장() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        
        //when
        memberRepository.save(member);
        Long id = member.getId();
        clear();
        
        //then
        Member findMember = em.find(Member.class, id);
        
        assertEquals("member == findMember", member, findMember);
    }
    
    @Test
    public void PK로검색() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        Member findMember = memberRepository.findById(id).orElse(null);
        
        //then
        assertEquals("member == findMember", member, findMember);
    }
    
    @Test
    public void PK로검색_없는PK() {
        //given
        long id = new Random().nextLong();
        
        //when
        Optional<Member> findMember = memberRepository.findById(id);
        
        //then
        assertFalse("findMember is empty", findMember.isPresent());
    }
    
    @Test
    public void 아이디로검색() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        Member findMember = memberRepository.findByUsername(member.getUsername()).orElse(null);
        
        //then
        assertEquals("member == findMember", member, findMember);
    }
    
    @Test
    public void 아이디로검색_없는아이디() {
        //given
        String username = UUID.randomUUID().toString();
        
        //when
        Optional<Member> findMember = memberRepository.findByUsername(username);
        
        //then
        assertFalse("findMember is empty", findMember.isPresent());
    }
    
    @Test
    public void 아이디존재여부() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        boolean exists = memberRepository.existsByUsername(member.getUsername());
        
        //then
        assertTrue("member exist", exists);
    }
    
    @Test
    public void 아이디존재여부_없는아이디() {
        //given
        String username = UUID.randomUUID().toString();
        
        //when
        boolean exists = memberRepository.existsByUsername(username);
        
        //then
        assertFalse("member does not exist", exists);
    }
    
    @Test
    public void 닉네임존재여부() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        boolean exists = memberRepository.existsByNickname(member.getNickname());
        
        //then
        assertTrue("member exist", exists);
    }
    
    @Test
    public void 닉네임존재여부_없는닉네임() {
        //given
        String nickname = UUID.randomUUID().toString();
        
        //when
        boolean exists = memberRepository.existsByNickname(nickname);
        
        //then
        assertFalse("member does not exist", exists);
    }
    
    @Test
    public void 수정() {
        //given
        String authToken = UUID.randomUUID().toString();
        Member member    = createMember("user@mail.com", "User12!@", authToken, "사용자");
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        Member findMember = memberRepository.findById(id).orElse(null);
        
        findMember.updateNickname("닉네임");
        findMember.updateSignature("서명입니다.");
        String newAuthToken = UUID.randomUUID().toString();
        findMember.updateEmailAuthToken(newAuthToken);
        findMember.authenticateEmail();
        findMember.updatePassword("User21@!");
        
        clear();
        
        //then
        Member updatedMember = em.find(Member.class, id);
        
        assertNotEquals("member != updatedMember", member, updatedMember);
        assertEquals("Nickname must be changed", "닉네임", updatedMember.getNickname());
        assertNotEquals("Nickname must not be same as before", member.getNickname(), updatedMember.getNickname());
        assertEquals("Signature must be changed", "서명입니다.", updatedMember.getSignature());
        assertNotEquals("Signature must not be same as before", member.getSignature(), updatedMember.getSignature());
        assertEquals("Password must be changed", "User21@!", updatedMember.getPassword());
        assertNotEquals("Password must not be same as before", member.getPassword(), updatedMember.getPassword());
        assertEquals("Auth token must be changed", newAuthToken, updatedMember.getEmailAuth().getAuthToken());
        assertNotEquals("Auth token must not be same as before",
                        authToken,
                        updatedMember.getEmailAuth().getAuthToken());
    }
    
    @Test
    public void 삭제() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        Member findMember = em.find(Member.class, id);
        memberRepository.delete(findMember);
        clear();
        
        //then
        assertFalse("Member must not be found", memberRepository.findById(id).isPresent());
    }
    
    @Test
    public void 이메일인증정보로검색() {
        //given
        Member member = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        em.persist(member);
        Long id = member.getId();
        clear();
        
        //when
        Member findMember = memberRepository.confirmEmailAuth(member.getUsername(),
                                                              member.getEmailAuth().getAuthToken(),
                                                              LocalDateTime.now()).orElse(null);
        
        //then
        assertEquals("member == findMember", member, findMember);
    }
    
    private Member createMember(String username, String password, String authToken, String nickname) {
        return Member.builder()
                     .username(username)
                     .password(password)
                     .emailAuth(EmailAuth.builder().authToken(authToken).build())
                     .nickname(nickname)
                     .build();
    }
    
}
