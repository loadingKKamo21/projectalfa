package com.project.alfa.repository;

import com.project.alfa.domain.Category;
import com.project.alfa.domain.Post;
import com.project.alfa.repository.querydsl.PostRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    
    Page<Post> findAllByNoticeYnOrderByCreatedDateDesc(Boolean noticeYn, Pageable pageable);
    
    Page<Post> findAllByCategoryAndNoticeYnOrderByCreatedDateDesc(Category category,
                                                                  Boolean noticeYn,
                                                                  Pageable pageable);
    
    boolean existsById(Long id);
    
}
