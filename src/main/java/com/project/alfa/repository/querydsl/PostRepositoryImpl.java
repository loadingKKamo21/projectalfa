package com.project.alfa.repository.querydsl;

import com.project.alfa.domain.Category;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.Post;
import com.project.alfa.domain.QPost;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    /**
     * 키워드가 검색된 목록 조회
     *
     * @param condition - 검색 조건
     * @param keyword   - 키워드
     * @param noticeYn  - 공지글 여부
     * @param pageable  - 페이징 객체
     * @return 검색된 게시글 목록
     */
    @Override
    public Page<Post> searchByKeyword(String condition, String keyword, Boolean noticeYn, Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(QPost.post)
                                         .where(searchEq(condition, keyword), QPost.post.noticeYn.eq(noticeYn))
                                         .orderBy(QPost.post.createdDate.desc())
                                         .offset(pageable.getOffset())
                                         .limit(pageable.getPageSize())
                                         .fetch();
        JPAQuery<Long> count = queryFactory.select(QPost.post.count())
                                           .from(QPost.post)
                                           .where(searchEq(condition, keyword), QPost.post.noticeYn.eq(noticeYn));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 키워드가 검색된 목록 조회
     *
     * @param category  - 카테고리
     * @param condition - 검색 조건
     * @param keyword   - 키워드
     * @param noticeYn  - 공지글 여부
     * @param pageable  - 페이징 객체
     * @return 검색된 게시글 목록
     */
    @Override
    public Page<Post> searchByKeyword(Category category,
                                      String condition,
                                      String keyword,
                                      Boolean noticeYn,
                                      Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(QPost.post)
                                         .where(QPost.post.category.eq(category),
                                                searchEq(condition, keyword),
                                                QPost.post.noticeYn.eq(noticeYn))
                                         .orderBy(QPost.post.createdDate.desc())
                                         .offset(pageable.getOffset())
                                         .limit(pageable.getPageSize())
                                         .fetch();
        JPAQuery<Long> count = queryFactory.select(QPost.post.count())
                                           .from(QPost.post)
                                           .where(QPost.post.category.eq(category),
                                                  searchEq(condition, keyword),
                                                  QPost.post.noticeYn.eq(noticeYn));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 조회할 갯수, 기간에 따른 목록 조회
     *
     * @param total  - 조회할 갯수
     * @param period - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *               문자열 입력 예시
     *               1. 3d -> 3일 전 ~ 오늘
     *               2. 4w -> 4주 전 ~ 오늘
     *               3. 1y -> 1년 전 ~ 오늘
     *               4. 0m -> 해석 불가
     *               period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriod(int total, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(periodEq(period))
                           .orderBy(QPost.post.createdDate.desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 작성자, 조회할 갯수, 기간에 따른 목록 조회
     *
     * @param writer - 작성자
     * @param total  - 조회할 갯수
     * @param period - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *               문자열 입력 예시
     *               1. 3d -> 3일 전 ~ 오늘
     *               2. 4w -> 4주 전 ~ 오늘
     *               3. 1y -> 1년 전 ~ 오늘
     *               4. 0m -> 해석 불가
     *               period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriod(Member writer, int total, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(QPost.post.writer.eq(writer), periodEq(period))
                           .orderBy(QPost.post.createdDate.desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 조회할 갯수, 공지글 여부, 기간에 따른 목록 조회
     *
     * @param total    - 조회할 갯수
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriod(int total, Boolean noticeYn, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(QPost.post.noticeYn.eq(noticeYn), periodEq(period))
                           .orderBy(QPost.post.createdDate.desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 작성자, 조회할 갯수, 공지글 여부, 기간에 따른 목록 조회
     *
     * @param writer   - 작성자
     * @param total    - 조회할 갯수
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriod(Member writer, int total, Boolean noticeYn, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(QPost.post.writer.eq(writer), QPost.post.noticeYn.eq(noticeYn), periodEq(period))
                           .orderBy(QPost.post.createdDate.desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 카테고리, 조회할 갯수, 공지글 여부, 기간에 따른 목록 조회
     *
     * @param category - 카테고리
     * @param total    - 조회할 갯수
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriod(Category category, int total, Boolean noticeYn, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(QPost.post.category.eq(category), QPost.post.noticeYn.eq(noticeYn), periodEq(period))
                           .orderBy(QPost.post.createdDate.desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 작성자, 카테고리, 조회할 갯수, 공지글 여부, 기간에 따른 목록 조회
     *
     * @param writer   - 작성자
     * @param category - 카테고리
     * @param total    - 조회할 갯수
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriod(Member writer, Category category, int total, Boolean noticeYn, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(QPost.post.writer.eq(writer),
                                  QPost.post.category.eq(category),
                                  QPost.post.noticeYn.eq(noticeYn),
                                  periodEq(period))
                           .orderBy(QPost.post.createdDate.desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 조회할 갯수, 기간에 따른 조회수 상위 목록 조회
     *
     * @param total  - 조회할 갯수
     * @param period - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *               문자열 입력 예시
     *               1. 3d -> 3일 전 ~ 오늘
     *               2. 4w -> 4주 전 ~ 오늘
     *               3. 1y -> 1년 전 ~ 오늘
     *               4. 0m -> 해석 불가
     *               period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriodAndViewCount(int total, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(periodEq(period))
                           .orderBy(QPost.post.viewCount.desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 조회할 갯수, 기간에 따른 댓글 수 상위 목록 조회
     *
     * @param total  - 조회할 갯수
     * @param period - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *               문자열 입력 예시
     *               1. 3d -> 3일 전 ~ 오늘
     *               2. 4w -> 4주 전 ~ 오늘
     *               3. 1y -> 1년 전 ~ 오늘
     *               4. 0m -> 해석 불가
     *               period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @return 조회된 게시글 목록
     */
    @Override
    public List<Post> findTopNByPeriodAndCommentCount(int total, String period) {
        return queryFactory.selectFrom(QPost.post)
                           .where(periodEq(period))
                           .orderBy(QPost.post.comments.size().desc())
                           .limit(total)
                           .fetch();
    }
    
    /**
     * 기간에 따른 목록 조회
     *
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @param pageable - 페이징 객체
     * @return 조회된 게시글 목록
     */
    @Override
    public Page<Post> findByPeriod(String period, Pageable pageable) {
        QueryResults<Post> results = queryFactory.selectFrom(QPost.post)
                                                 .where(periodEq(period))
                                                 .orderBy(QPost.post.createdDate.desc())
                                                 .offset(pageable.getOffset())
                                                 .limit(pageable.getPageSize())
                                                 .fetchResults();
        List<Post> content = results.getResults();
        long       total   = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }
    
    /**
     * 작성자, 기간에 따른 목록 조회
     *
     * @param writer   - 작성자
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @param pageable - 페이징 객체
     * @return 조회된 게시글 목록
     */
    @Override
    public Page<Post> findByPeriod(Member writer, String period, Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(QPost.post)
                                         .where(QPost.post.writer.eq(writer), periodEq(period))
                                         .orderBy(QPost.post.createdDate.desc())
                                         .offset(pageable.getOffset())
                                         .limit(pageable.getPageSize())
                                         .fetch();
        JPAQuery<Long> count = queryFactory.select(QPost.post.count())
                                           .from(QPost.post)
                                           .where(QPost.post.writer.eq(writer), periodEq(period));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 공지글 여부, 기간에 따른 목록 조회
     *
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @param pageable - 페이징 객체
     * @return 조회된 게시글 목록
     */
    @Override
    public Page<Post> findByPeriod(Boolean noticeYn, String period, Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(QPost.post)
                                         .where(QPost.post.noticeYn.eq(noticeYn), periodEq(period))
                                         .orderBy(QPost.post.createdDate.desc())
                                         .offset(pageable.getOffset())
                                         .limit(pageable.getPageSize())
                                         .fetch();
        JPAQuery<Long> count = queryFactory.select(QPost.post.count())
                                           .from(QPost.post)
                                           .where(QPost.post.noticeYn.eq(noticeYn), periodEq(period));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 작성자, 공지글 여부, 기간에 따른 목록 조회
     *
     * @param writer   - 작성자
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @param pageable - 페이징 객체
     * @return 조회된 게시글 목록
     */
    @Override
    public Page<Post> findByPeriod(Member writer, Boolean noticeYn, String period, Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(QPost.post)
                                         .where(QPost.post.writer.eq(writer),
                                                QPost.post.noticeYn.eq(noticeYn),
                                                periodEq(period))
                                         .orderBy(QPost.post.createdDate.desc())
                                         .offset(pageable.getOffset())
                                         .limit(pageable.getPageSize())
                                         .fetch();
        JPAQuery<Long> count = queryFactory.select(QPost.post.count())
                                           .from(QPost.post)
                                           .where(QPost.post.writer.eq(writer),
                                                  QPost.post.noticeYn.eq(noticeYn),
                                                  periodEq(period));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 공지글 여부, 기간에 따른 목록 조회
     *
     * @param category - 카테고리
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @param pageable - 페이징 객체
     * @return 조회된 게시글 목록
     */
    @Override
    public Page<Post> findByPeriod(Category category, Boolean noticeYn, String period, Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(QPost.post)
                                         .where(QPost.post.category.eq(category),
                                                QPost.post.noticeYn.eq(noticeYn),
                                                periodEq(period))
                                         .orderBy(QPost.post.createdDate.desc())
                                         .offset(pageable.getOffset())
                                         .limit(pageable.getPageSize())
                                         .fetch();
        JPAQuery<Long> count = queryFactory.select(QPost.post.count())
                                           .from(QPost.post)
                                           .where(QPost.post.category.eq(category),
                                                  QPost.post.noticeYn.eq(noticeYn),
                                                  periodEq(period));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 작성자, 공지글 여부, 기간에 따른 목록 조회
     *
     * @param writer   - 작성자
     * @param category - 카테고리
     * @param noticeYn - 공지글 여부
     * @param period   - 기간: 숫자 + d(일), w(주), m(월), y(년)
     *                 문자열 입력 예시
     *                 1. 3d -> 3일 전 ~ 오늘
     *                 2. 4w -> 4주 전 ~ 오늘
     *                 3. 1y -> 1년 전 ~ 오늘
     *                 4. 0m -> 해석 불가
     *                 period의 문자열 형태가 올바르지 않거나, null 값인 경우 기간 미설정(전 기간)
     * @param pageable - 페이징 객체
     * @return 조회된 게시글 목록
     */
    @Override
    public Page<Post> findByPeriod(Member writer,
                                   Category category,
                                   Boolean noticeYn,
                                   String period,
                                   Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(QPost.post)
                                         .where(QPost.post.writer.eq(writer),
                                                QPost.post.category.eq(category),
                                                QPost.post.noticeYn.eq(noticeYn),
                                                periodEq(period))
                                         .orderBy(QPost.post.createdDate.desc())
                                         .offset(pageable.getOffset())
                                         .limit(pageable.getPageSize())
                                         .fetch();
        JPAQuery<Long> count = queryFactory.select(QPost.post.count())
                                           .from(QPost.post)
                                           .where(QPost.post.writer.eq(writer),
                                                  QPost.post.category.eq(category),
                                                  QPost.post.noticeYn.eq(noticeYn),
                                                  periodEq(period));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
    }
    
    /**
     * 검색 조건, 키워드에 따른 조건
     * 1. 제목에 키워드 포함
     * 2. 내용에 키워드 포함
     * 3. 제목 또는 내용에 키워드 포함
     * 4. 작성자 닉네임에 키워드 포함
     * 5. 제목, 내용, 작성자 닉네임에 키워드 포함
     *
     * @param condition - 검색 조건
     * @param keyword   - 키워드
     * @return
     */
    private BooleanExpression searchEq(String condition, String keyword) {
        if (hasText(condition)) if (condition.equals("title"))  //제목
            return QPost.post.title.like("%" + keyword + "%");
        else if (condition.equals("content"))   //내용
            return QPost.post.content.like("%" + keyword + "%");
        else if (condition.equals("titleOrContent"))    //제목+내용
            return QPost.post.title.like("%" + keyword + "%").or(QPost.post.content.like("%" + keyword + "%"));
        else if (condition.equals("writer"))    //작성자
            return QPost.post.writer.nickname.like("%" + keyword + "%");
        return QPost.post.title.like("%" + keyword + "%")
                               .or(QPost.post.content.like("%" + keyword + "%"))
                               .or(QPost.post.writer.nickname.like("%" + keyword + "%"));
    }
    
    /**
     * 기간에 따른 조건
     * 입력된 문자열을 기준으로 오늘 날짜로부터 얼마 전까지의 기간 동안 작성된 것을 조회할 지 조건 반환
     *
     * @param period - 기간: 숫자 + d(일), w(주), m(월), y(년)
     * @return
     */
    private BooleanExpression periodEq(String period) {
        if (hasText(period)) {
            final String REGEX = "^[0-9]+[dwmy]{1}";    //숫자 + d(일), w(주), m(월), y(년) 정규표현식
            
            if (period.matches(REGEX)) {
                long num = Long.parseLong(period.substring(0, period.length() - 1));
                
                if (num == 0) return null;  //숫자가 0일 때
                
                char c = period.charAt(period.length() - 1);
                
                LocalDate today = LocalDate.now();
                
                if (c == 'd')   //몇 '일'전 까지의 기간
                    return QPost.post.createdDate.after(today.minusDays(num).atStartOfDay());
                else if (c == 'w')  //몇 '주'전 까지의 기간
                    return QPost.post.createdDate.after(today.minusWeeks(num).atStartOfDay());
                else if (c == 'm')  //몇 '달'전 까지의 기간
                    return QPost.post.createdDate.after(today.minusMonths(num).atStartOfDay());
                else if (c == 'y')  //몇 '년'전 까지의 기간
                    return QPost.post.createdDate.after(today.minusYears(num).atStartOfDay());
            }
        }
        return null;
    }
    
}
