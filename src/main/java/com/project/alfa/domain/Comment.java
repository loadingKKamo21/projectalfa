package com.project.alfa.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "tb_comments")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class Comment extends BaseTimeEntity {
    
    @Id
    @GeneratedValue
    @Column(name = "comment_id")
    private Long id;        //PK
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;  //작성자
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;      //게시글
    
    @Column(nullable = false)
    private String content; //내용
    
    @Builder
    public Comment(Member writer, Post post, String content) {
        setWriter(writer);
        setPost(post);
        this.content = content;
    }
    
    //==================== 연관관계 메서드 ====================//
    
    public void setWriter(Member writer) {
        this.writer = writer;
        writer.getComments().add(this);
    }
    
    public void setPost(Post post) {
        this.post = post;
        post.getComments().add(this);
    }
    
    //==================== 댓글 내용 수정 메서드 ====================//
    
    /**
     * 내용 수정
     *
     * @param newContent - 수정할 내용
     */
    public void updateContent(String newContent) {
        content = newContent;
    }
    
}
