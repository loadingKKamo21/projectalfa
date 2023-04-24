package com.project.alfa.service.dto;

import com.project.alfa.domain.Post;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class PostListResponseDto implements Serializable {
    
    private final Long          id;
    private final String        wNickname;
    private final String        title;
    private final String        category;
    private final Boolean       noticeYn;
    private final int           viewCount;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    private final int           commentsCount;
    
    public PostListResponseDto(Post post) {
        id = post.getId();
        wNickname = post.getWriter().getNickname();
        title = post.getTitle();
        category = categoryToString(post.getCategory().getName());
        noticeYn = post.getNoticeYn();
        viewCount = post.getViewCount();
        createdDate = post.getCreatedDate();
        lastModifiedDate = post.getLastModifiedDate();
        commentsCount = post.getComments().size();
    }
    
    private String categoryToString(String category) {
        String name;
        switch (category) {
            case "ALL":
                name = "-";
                break;
            case "FREE":
                name = "자유";
                break;
            case "QNA":
                name = "질문";
                break;
            default:
                name = "-";
                break;
        }
        return name;
    }
    
}
