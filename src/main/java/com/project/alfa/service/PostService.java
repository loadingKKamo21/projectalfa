package com.project.alfa.service;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.ErrorCode;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.domain.Post;
import com.project.alfa.repository.CategoryRepository;
import com.project.alfa.repository.MemberRepository;
import com.project.alfa.repository.PostRepository;
import com.project.alfa.service.dto.PostListResponseDto;
import com.project.alfa.service.dto.PostReadResponseDto;
import com.project.alfa.service.dto.PostWriteRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
public class PostService {
    
    private final PostRepository     postRepository;
    private final MemberRepository   memberRepository;
    private final CategoryRepository categoryRepository;
    private final CacheManager       cacheManager;
    
    /**
     * 게시글 저장
     *
     * @param dto - 게시글 정보
     * @return 게시글 PK
     */
    @Transactional
    public Long save(final PostWriteRequestDto dto) {
        Post post = postRepository.save(Post.builder()
                                            .writer(memberRepository.findById(dto.getWId())
                                                                    .orElseThrow(() -> new EntityNotFoundException(
                                                                            "Could not found 'Member' entity by id: " + dto.getWId())))
                                            .title(dto.getTitle())
                                            .content(dto.getContent())
                                            .category(categoryRepository.findById(dto.getCId())
                                                                        .orElseThrow(() -> new EntityNotFoundException(
                                                                                "Could not found 'Category' entity by id: " + dto.getCId())))
                                            .noticeYn(dto.getNoticeYn())
                                            .build());
        return post.getId();
    }
    
    /**
     * 게시글 상세 조회(@Cacheable)
     *
     * @param id - 게시글 PK
     * @return 게시글 상세 정보
     */
    //    @Cacheable(value = "postCache", unless = "#result == null", key = "{#id, #sessionId, #ipAddress}")
    @Cacheable(value = "postCache", unless = "#result == null", keyGenerator = "customKeyGenerator")
    public PostReadResponseDto readPost(final Long id, final String sessionId, final String ipAddress) {
        return new PostReadResponseDto(postRepository.findById(id)
                                                     .orElseThrow(() -> new EntityNotFoundException(
                                                             "Could not found 'Post' entity by id: " + id)));
    }
    
    /**
     * 게시글 정보 조회
     *
     * @param id - 게시글 PK
     * @return 게시글 상세 정보
     */
    public PostReadResponseDto readPost(final Long id) {
        return new PostReadResponseDto(postRepository.findById(id)
                                                     .orElseThrow(() -> new EntityNotFoundException(
                                                             "Could not found 'Post' entity by id: " + id)));
    }
    
    /**
     * 게시글 조회수 증가(@CachePut)
     *
     * @param id - 게시글 PK
     */
    //    @CachePut(value = "postCache", unless = "#post == null", key = "{#id, #sessionId, #ipAddress}")
    @CachePut(value = "postCache", unless = "#post == null", keyGenerator = "customKeyGenerator")
    @Transactional
    public void updateViewCount(final Long id, final String sessionId, final String ipAddress) {
        Post post = postRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException("Could not found 'Post' entity by id: " + id));
        if (!isPostCached(id, sessionId, ipAddress)) post.addViewCount();   //캐시 값이 없다면 조회수 증가
    }
    
    /**
     * 게시글 목록 조회
     *
     * @param categoryName - 카테고리 명
     * @param noticeYn     - 공지글 여부
     * @param pageable     - 페이징 객체
     * @return 게시글 목록
     */
    @Cacheable(value = "postList", unless = "#result == null || #result.totalElements > 0", key = "{#categoryName, #noticeYn, #pageable, #result}")
    public Page<PostListResponseDto> findAll(final String categoryName, final Boolean noticeYn, Pageable pageable) {
        if (categoryName.equals("ALL"))
            return postRepository.findAllByNoticeYnOrderByCreatedDateDesc(noticeYn, pageable)
                                 .map(PostListResponseDto::new);
        else return postRepository.findAllByCategoryAndNoticeYnOrderByCreatedDateDesc(categoryRepository.findByName(
                                                                                                                categoryName)
                                                                                                        .orElseThrow(() -> new EntityNotFoundException(
                                                                                                                "Could not found 'Category' entity by name: " + categoryName)),
                                                                                      noticeYn,
                                                                                      pageable)
                                  .map(PostListResponseDto::new);
    }
    
    /**
     * 게시글 검색 목록 조회
     *
     * @param categoryName - 카테고리 명
     * @param condition    - 검색 조건
     * @param keyword      - 키워드
     * @param noticeYn     - 공지글 여부
     * @param pageable     - 페이징 객체
     * @return 검색된 게시글 목록
     */
    @Cacheable(value = "searchList", unless = "#result == null || #result.totalElements > 0", key = "{#categoryName, #condition, #keyword, #noticeYn, #pageable, #result}")
    public Page<PostListResponseDto> findAllWithKeyword(final String categoryName,
                                                        final String condition,
                                                        final String keyword,
                                                        final Boolean noticeYn,
                                                        Pageable pageable) {
        if (categoryName.equals("ALL"))
            return postRepository.searchByKeyword(condition, keyword, noticeYn, pageable).map(PostListResponseDto::new);
        else return postRepository.searchByKeyword(categoryRepository.findByName(categoryName)
                                                                     .orElseThrow(() -> new EntityNotFoundException(
                                                                             "Could not found 'Category' entity by name: " + categoryName)),
                                                   condition,
                                                   keyword,
                                                   noticeYn,
                                                   pageable).map(PostListResponseDto::new);
    }
    
    /**
     * 최근 공지글 N개 목록 조회
     * 카테고리가 "ALL"이 아닌 경우 해당 카테고리 기준 최근 공지글 N개 조회
     * 카테고리가 "ALL"인 경우 전체 카테고리 기준 최근 공지글 N개 조회
     *
     * @param categoryName - 카테고리 명
     * @param total        - 조회할 갯수(N)
     * @return 최근 공지글 N개 목록
     */
    public List<PostListResponseDto> findTopNNotice(final String categoryName, final int total) {
        if (categoryName.equals("ALL")) return postRepository.findTopNByPeriod(total, true, null)
                                                             .stream()
                                                             .map(PostListResponseDto::new)
                                                             .collect(toList());
        else return postRepository.findTopNByPeriod(categoryRepository.findByName(categoryName)
                                                                      .orElseThrow(() -> new EntityNotFoundException(
                                                                              "Could not found 'Category' entity by name: " + categoryName)),
                                                    total,
                                                    true,
                                                    null).stream().map(PostListResponseDto::new).collect(toList());
    }
    
    /**
     * 게시글 수정
     *
     * @param dto - 게시글 정보
     */
    @Transactional
    public void update(final PostWriteRequestDto dto) {
        Post post = postRepository.findById(dto.getId())
                                  .orElseThrow(() -> new EntityNotFoundException("Could not found 'Post' entity by id: " + dto.getId()));
        
        if (!memberRepository.existsById(dto.getWId()))
            throw new EntityNotFoundException("Could not found 'Member' entity by id: " + dto.getWId());
        //작성자 확인
        if (!post.getWriter().getId().equals(dto.getWId()))
            throw new InvalidValueException("Not writer of post", ErrorCode.NOT_WRITER_OF_POST);
        
        post.updateTitle(dto.getTitle());
        post.updateContent(dto.getContent());
        post.updateCategory(categoryRepository.findById(dto.getCId())
                                              .orElseThrow(() -> new EntityNotFoundException(
                                                      "Could not found 'Category' entity by id: " + dto.getCId())));
        post.updateNoticeYn(dto.getNoticeYn());
    }
    
    /**
     * 게시글 삭제
     *
     * @param writerId - 작성자 PK
     * @param id       - 게시글 PK
     */
    @Transactional
    public void delete(final Long writerId, final Long id) {
        Post post = postRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException("Could not found 'Post' entity by id: " + id));
        
        if (!memberRepository.existsById(writerId))
            throw new EntityNotFoundException("Could not found 'Member' entity by id: " + writerId);
        if (!post.getWriter().getId().equals(writerId))
            throw new InvalidValueException("Not writer of post", ErrorCode.NOT_WRITER_OF_POST);
        
        postRepository.deleteById(id);
    }
    
    /**
     * 오늘 작성된 게시글 중 조회수 상위 N개 목록 조회
     *
     * @param total - 조회할 갯수(N)
     * @return 게시글 N개 목록
     */
    public List<PostListResponseDto> findTopNViewCountToday(final int total) {
        return postRepository.findTopNByPeriodAndViewCount(total, "1d")
                             .stream()
                             .map(PostListResponseDto::new)
                             .collect(toList());
    }
    
    /**
     * 오늘 작성된 게시글 중 댓글 수 상위 N개 목록 조회
     *
     * @param total - 조회할 갯수(N)
     * @return 게시글 N개 목록
     */
    public List<PostListResponseDto> findTopNCommentCountToday(final int total) {
        return postRepository.findTopNByPeriodAndCommentCount(total, "1d")
                             .stream()
                             .map(PostListResponseDto::new)
                             .collect(toList());
    }
    
    /**
     * 신규 게시글 N개 목록 조회
     *
     * @param total - 조회할 갯수(N)
     * @return 게시글 N개 목록
     */
    public List<PostListResponseDto> findTopNNewPosts(final int total) {
        return postRepository.findTopNByPeriod(total, null).stream().map(PostListResponseDto::new).collect(toList());
    }
    
    /**
     * 게시글 상세 조회 캐싱 여부 확인
     *
     * @param id        - 게시글 PK
     * @param sessionId - 세션 ID
     * @param ipAddress - IP 주소
     * @return 캐시 값 존재 여부
     */
    private boolean isPostCached(final Long id, final String sessionId, final String ipAddress) {
        Cache postCache = cacheManager.getCache("postCache");
        if (postCache == null) return false;
        return postCache.get(id.toString() + "," + sessionId + "," + ipAddress) != null;    //customKeyGenerator
    }
    
}
