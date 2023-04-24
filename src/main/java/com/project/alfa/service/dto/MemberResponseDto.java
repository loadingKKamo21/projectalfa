package com.project.alfa.service.dto;

import com.project.alfa.domain.Comment;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Getter
public class MemberResponseDto {
    
    private final Long                      id;
    private final String                    username;
    private final String                    nickname;
    private final String                    signature;
    private final String                    image64;
    private final String                    role;
    private final LocalDateTime             createdDate;
    private final LocalDateTime             lastModifiedDate;
    private final String                    provider;
    private final String                    providerId;
    private final List<PostReadResponseDto> posts;
    private final List<CommentResponseDto>  comments;
    private final int                       postsCount;
    private final int                       commentsCount;
    
    public MemberResponseDto(Member member) {
        id = member.getId();
        username = member.getUsername();
        nickname = member.getNickname();
        signature = member.getSignature();
        image64 = member.getProfileImage() != null ? member.getProfileImage().getBase64Data() : null;
        role = member.getRole().toString();
        createdDate = member.getCreatedDate();
        lastModifiedDate = member.getLastModifiedDate();
        provider = member.getProvider();
        providerId = member.getProviderId();
        posts = member.getPosts().stream().map(PostReadResponseDto::new).collect(toList());
        comments = member.getComments().stream().map(CommentResponseDto::new).collect(toList());
        postsCount = member.getPosts().size();
        commentsCount = member.getComments().size();
    }
    
    public MemberResponseDto(Member member, List<Post> postList, List<Comment> commentsList) {
        id = member.getId();
        username = member.getUsername();
        nickname = member.getNickname();
        signature = member.getSignature();
        image64 = member.getProfileImage() != null ? member.getProfileImage().getBase64Data() : null;
        role = member.getRole().toString();
        createdDate = member.getCreatedDate();
        lastModifiedDate = member.getLastModifiedDate();
        provider = member.getProvider();
        providerId = member.getProviderId();
        posts = postList.stream().map(PostReadResponseDto::new).collect(toList());
        comments = commentsList.stream().map(CommentResponseDto::new).collect(toList());
        postsCount = member.getPosts().size();
        commentsCount = member.getComments().size();
    }
    
}
