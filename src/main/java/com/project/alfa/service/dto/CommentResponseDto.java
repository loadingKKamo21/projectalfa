package com.project.alfa.service.dto;

import com.project.alfa.domain.Comment;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class CommentResponseDto implements Serializable {
    
    private final Long          id;
    private final Long          pId;
    private final String        pTitle;
    private final Long          wId;
    private final String        wNickname;
    private final String        content;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    
    public CommentResponseDto(Comment comment) {
        id = comment.getId();
        pId = comment.getPost().getId();
        pTitle = comment.getPost().getTitle();
        wId = comment.getWriter().getId();
        wNickname = comment.getWriter().getNickname();
        content = comment.getContent();
        createdDate = comment.getCreatedDate();
        lastModifiedDate = comment.getLastModifiedDate();
    }
    
}
