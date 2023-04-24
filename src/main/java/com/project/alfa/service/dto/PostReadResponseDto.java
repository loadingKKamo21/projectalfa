package com.project.alfa.service.dto;

import com.project.alfa.domain.Post;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Getter
public class PostReadResponseDto implements Serializable {
    private final Long                     id;
    private final Long                     wId;
    private final String                   wNickname;
    private final String                   title;
    private final String                   content;
    private final Long                     cId;
    private final String                   category;
    private final Boolean                  noticeYn;
    private final int                      viewCount;
    private final LocalDateTime            createdDate;
    private final LocalDateTime            lastModifiedDate;
    private final List<CommentResponseDto> comments;
    private final int                      commentsCount;
    
    public PostReadResponseDto(Post post) {
        id = post.getId();
        wId = post.getWriter().getId();
        wNickname = post.getWriter().getNickname();
        title = post.getTitle();
        content = post.getContent();
        cId = post.getCategory().getId();
        category = categoryToString(post.getCategory().getName());
        noticeYn = post.getNoticeYn();
        viewCount = post.getViewCount();
        createdDate = post.getCreatedDate();
        lastModifiedDate = post.getLastModifiedDate();
        comments = post.getComments().stream().map(CommentResponseDto::new).collect(toList());
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
