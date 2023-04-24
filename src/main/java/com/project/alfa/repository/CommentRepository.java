package com.project.alfa.repository;

import com.project.alfa.domain.Comment;
import com.project.alfa.domain.Post;
import com.project.alfa.repository.querydsl.CommentRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    
    Page<Comment> findAllByPostOrderByCreatedDateDesc(Post post, Pageable pageable);
    
}
