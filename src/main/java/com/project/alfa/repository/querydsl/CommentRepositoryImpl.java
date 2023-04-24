package com.project.alfa.repository.querydsl;

import com.project.alfa.domain.Comment;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.QComment;
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
public class CommentRepositoryImpl implements CommentRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
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
     * @return 조회된 댓글 목록
     */
    @Override
    public List<Comment> findTopNByPeriod(int total, String period) {
        return queryFactory.selectFrom(QComment.comment)
                           .where(periodEq(period))
                           .orderBy(QComment.comment.createdDate.desc())
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
     * @return 조회된 댓글 목록
     */
    @Override
    public List<Comment> findTopNByPeriod(Member writer, int total, String period) {
        return queryFactory.selectFrom(QComment.comment)
                           .where(QComment.comment.writer.eq(writer), periodEq(period))
                           .orderBy(QComment.comment.createdDate.desc())
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
     * @return 조회된 댓글 목록
     */
    @Override
    public Page<Comment> findByPeriod(String period, Pageable pageable) {
        QueryResults<Comment> results = queryFactory.selectFrom(QComment.comment)
                                                    .where(periodEq(period))
                                                    .orderBy(QComment.comment.createdDate.desc())
                                                    .offset(pageable.getOffset())
                                                    .limit(pageable.getPageSize())
                                                    .fetchResults();
        List<Comment> content = results.getResults();
        long          total   = results.getTotal();
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
     * @return 조회된 댓글 목록
     */
    @Override
    public Page<Comment> findByPeriod(Member writer, String period, Pageable pageable) {
        List<Comment> content = queryFactory.selectFrom(QComment.comment)
                                            .where(QComment.comment.writer.eq(writer), periodEq(period))
                                            .orderBy(QComment.comment.createdDate.desc())
                                            .offset(pageable.getOffset())
                                            .limit(pageable.getPageSize())
                                            .fetch();
        JPAQuery<Long> count = queryFactory.select(QComment.comment.count())
                                           .from(QComment.comment)
                                           .where(QComment.comment.writer.eq(writer), periodEq(period));
        return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
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
                    return QComment.comment.createdDate.after(today.minusDays(num).atStartOfDay());
                else if (c == 'w')  //몇 '주'전 까지의 기간
                    return QComment.comment.createdDate.after(today.minusWeeks(num).atStartOfDay());
                else if (c == 'm')  //몇 '달'전 까지의 기간
                    return QComment.comment.createdDate.after(today.minusMonths(num).atStartOfDay());
                else if (c == 'y')  //몇 '년'전 까지의 기간
                    return QComment.comment.createdDate.after(today.minusYears(num).atStartOfDay());
            }
        }
        return null;
    }
    
}
