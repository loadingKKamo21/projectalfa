package com.project.alfa.repository.querydsl;

import com.project.alfa.domain.Category;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {
    
    Page<Post> searchByKeyword(String condition, String keyword, Boolean noticeYn, Pageable pageable);
    
    Page<Post> searchByKeyword(Category category,
                               String condition,
                               String keyword,
                               Boolean noticeYn,
                               Pageable pageable);
    
    List<Post> findTopNByPeriod(int total, String period);
    
    List<Post> findTopNByPeriod(Member writer, int total, String period);
    
    List<Post> findTopNByPeriod(int total, Boolean noticeYn, String period);
    
    List<Post> findTopNByPeriod(Member writer, int total, Boolean noticeYn, String period);
    
    List<Post> findTopNByPeriod(Category category, int total, Boolean noticeYn, String period);
    
    List<Post> findTopNByPeriod(Member writer, Category category, int total, Boolean noticeYn, String period);
    
    List<Post> findTopNByPeriodAndViewCount(int total, String period);
    
    List<Post> findTopNByPeriodAndCommentCount(int total, String period);
    
    Page<Post> findByPeriod(String period, Pageable pageable);
    
    Page<Post> findByPeriod(Member writer, String period, Pageable pageable);
    
    Page<Post> findByPeriod(Boolean noticeYn, String period, Pageable pageable);
    
    Page<Post> findByPeriod(Member writer, Boolean noticeYn, String period, Pageable pageable);
    
    Page<Post> findByPeriod(Category category, Boolean noticeYn, String period, Pageable pageable);
    
    Page<Post> findByPeriod(Member writer, Category category, Boolean noticeYn, String period, Pageable pageable);
    
}
