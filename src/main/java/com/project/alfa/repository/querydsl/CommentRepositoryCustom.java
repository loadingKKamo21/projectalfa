package com.project.alfa.repository.querydsl;

import com.project.alfa.domain.Comment;
import com.project.alfa.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepositoryCustom {
    
    List<Comment> findTopNByPeriod(int total, String period);
    
    List<Comment> findTopNByPeriod(Member writer, int total, String period);
    
    Page<Comment> findByPeriod(String period, Pageable pageable);
    
    Page<Comment> findByPeriod(Member writer, String period, Pageable pageable);
    
}
