package com.project.alfa.service;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.ErrorCode;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.domain.Comment;
import com.project.alfa.repository.CommentRepository;
import com.project.alfa.repository.MemberRepository;
import com.project.alfa.repository.PostRepository;
import com.project.alfa.service.dto.CommentRequestDto;
import com.project.alfa.service.dto.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final MemberRepository  memberRepository;
    private final PostRepository    postRepository;
    
    /**
     * 댓글 저장
     *
     * @param dto - 댓글 정보
     * @return 댓글 PK
     */
    @Transactional
    public Long save(final CommentRequestDto dto) {
        Comment comment = commentRepository.save(Comment.builder()
                                                        .writer(memberRepository.findById(dto.getWId())
                                                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                                        "Could not found 'Member' entity by id: " + dto.getWId())))
                                                        .post(postRepository.findById(dto.getPId())
                                                                            .orElseThrow(() -> new EntityNotFoundException(
                                                                                    "Could not found 'Post' entity by id: " + dto.getPId())))
                                                        .content(dto.getContent())
                                                        .build());
        return comment.getId();
    }
    
    /**
     * 게시글 내 댓글 목록 조회
     *
     * @param postId   - 게시글 PK
     * @param pageable - 페이징 객체
     * @return 댓글 목록
     */
    public Page<CommentResponseDto> readComments(final Long postId, Pageable pageable) {
        return commentRepository.findAllByPostOrderByCreatedDateDesc(postRepository.findById(postId)
                                                                                   .orElseThrow(() -> new EntityNotFoundException(
                                                                                           "Could not found 'Post' entity by id: " + postId)),
                                                                     pageable).map(CommentResponseDto::new);
    }
    
    /**
     * 댓글 수정
     *
     * @param dto - 댓글 정보
     */
    @Transactional
    public void update(final CommentRequestDto dto) {
        Comment comment = commentRepository.findById(dto.getId())
                                           .orElseThrow(() -> new EntityNotFoundException(
                                                   "Could not found 'Comment' entity by id: " + dto.getId()));
        
        //작성자 확인
        if (!comment.getWriter().getId().equals(dto.getWId()))
            throw new InvalidValueException("Not writer of comment", ErrorCode.NOT_WRITER_OF_COMMENT);
        
        comment.updateContent(dto.getContent());
    }
    
    /**
     * 댓글 삭제
     *
     * @param writerId - 작성자 PK
     * @param id       - 댓글 PK
     */
    @Transactional
    public void delete(final Long writerId, final Long id) {
        Comment comment = commentRepository.findById(id)
                                           .orElseThrow(() -> new EntityNotFoundException(
                                                   "Could not found 'Comment' entity by id: " + id));
        
        if (!memberRepository.existsById(writerId))
            throw new EntityNotFoundException("Could not found 'Member' entity by id: " + writerId);
        if (!comment.getWriter().getId().equals(writerId))
            throw new InvalidValueException("Not writer of comment", ErrorCode.NOT_WRITER_OF_COMMENT);
        
        commentRepository.deleteById(id);
    }
    
    /**
     * 신규 댓글 N개 목록 조회
     *
     * @param total - 조회할 갯수(N)
     * @return 댓글 N개 목록
     */
    public List<CommentResponseDto> findTopNNewComments(final int total) {
        return commentRepository.findTopNByPeriod(total, null).stream().map(CommentResponseDto::new).collect(toList());
    }
    
}
