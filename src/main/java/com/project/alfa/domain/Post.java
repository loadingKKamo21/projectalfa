package com.project.alfa.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "tb_posts")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(exclude = { "comments" }, callSuper = false)
public class Post extends BaseTimeEntity {
    
    @Id
    @GeneratedValue
    @Column(name = "post_id")
    private Long id;                                    //PK
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;                              //작성자
    
    @Column(nullable = false)
    @Size(min = 1, max = 100)
    private String title;                               //제목
    
    @Lob
    private String content;                             //내용
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;                          //카테고리
    
    @Column(nullable = false)
    private int viewCount;                              //조회수
    
    @Column(nullable = false)
    private Boolean noticeYn;                           //공지 여부
    
    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>(); //게시글 내 댓글 리스트
    
    @Builder
    public Post(Member writer, String title, String content, Category category, Boolean noticeYn) {
        setWriter(writer);
        this.title = title;
        this.content = content;
        setCategory(category);
        this.viewCount = 0;
        this.noticeYn = noticeYn;
    }
    
    //==================== 연관관계 메서드 ====================//
    
    public void setWriter(Member writer) {
        this.writer = writer;
        writer.getPosts().add(this);
    }
    
    public void setCategory(Category category) {
        if (this.category != null && this.category.getPosts().contains(this)) this.category.getPosts().remove(this);
        this.category = category;
        category.getPosts().add(this);
    }
    
    //==================== 게시글 수정 메서드 ====================//
    
    /**
     * 제목 수정
     *
     * @param newTitle - 수정할 제목
     */
    public void updateTitle(String newTitle) {
        if ((newTitle != null && !newTitle.trim().isEmpty()) && !title.equals(newTitle)) title = newTitle;
    }
    
    /**
     * 내용 수정
     *
     * @param newContent - 수정할 내용
     */
    public void updateContent(String newContent) {
        if ((newContent != null && !newContent.trim().isEmpty()) && !content.equals(newContent)) content = newContent;
    }
    
    /**
     * 카테고리 변경
     *
     * @param newCategory - 수정할 카테고리
     */
    public void updateCategory(Category newCategory) {
        if (newCategory != null && category != newCategory) setCategory(newCategory);
    }
    
    /**
     * 공지 여부 변경
     *
     * @param newNoticeYn - 수정할 공지 여부
     */
    public void updateNoticeYn(Boolean newNoticeYn) {
        if (newNoticeYn != null && noticeYn != newNoticeYn) noticeYn = newNoticeYn;
    }
    
    //==================== 조회수 증가 메서드 ====================//
    
    /**
     * 조회수 증가
     */
    public void addViewCount() {
        viewCount++;
    }
    
}
