package com.project.alfa.repository;

import com.project.alfa.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
    Optional<Member> findByUsername(String username);
    
    @Query("select m from Member m where m.username = :email and m.emailAuth.auth = false and m.emailAuth.authToken = :authToken and m.emailAuth.expireDate >= :currentTime")
    Optional<Member> confirmEmailAuth(@Param("email") String email,
                                      @Param("authToken") String authToken,
                                      @Param("currentTime") LocalDateTime currentTime);
    
    boolean existsByUsername(String username);
    
    boolean existsByNickname(String nickname);
    
}
