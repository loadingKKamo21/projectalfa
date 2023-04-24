package com.project.alfa.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "tb_categories")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(exclude = { "posts", "child" }, callSuper = false)
public class Category extends BaseTimeEntity {
    
    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;                                  //PK
    
    @Column(unique = true)
    private String name;                              //카테고리 명
    
    @OneToMany(mappedBy = "category")
    private List<Post> posts = new ArrayList<>();     //카테고리 내 게시글 리스트
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;                          //부모 카테고리
    
    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>(); //자식 카테고리 리스트
    
    @Builder
    public Category(String name) {
        this.name = name;
    }
    
    //==================== 부모-자식 카테고리 설정 메서드 ====================//
    
    public void setParent(Category parent) {
        this.parent = parent;
    }
    
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
    
    //==================== 카테고리 수정 메서드 ====================//
    
    /**
     * 카테고리 명 변경
     *
     * @param newName - 수정할 카테고리 명
     */
    public void updateCategoryName(String newName) {
        name = newName;
    }
    
}
