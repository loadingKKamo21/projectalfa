package com.project.alfa.repository;

import com.project.alfa.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CommentRepositoryTest {
    
    @Autowired
    CommentRepository commentRepository;
    @PersistenceContext
    EntityManager     em;
    
    @After
    public void clear() {
        em.flush();
        em.clear();
    }
    
    @Test
    public void 저장() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        em.persist(writer);
        em.persist(category);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        clear();
        
        Comment comment = createComment(writer, post, "댓글 내용");
        
        //when
        commentRepository.save(comment);
        Long id = comment.getId();
        clear();
        
        //then
        Comment findComment = em.find(Comment.class, id);
        
        assertEquals("comment == findComment", comment, findComment);
    }
    
    @Test
    public void PK로검색() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        em.persist(writer);
        em.persist(category);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        
        Comment comment = createComment(writer, post, "댓글 내용");
        em.persist(comment);
        Long id = comment.getId();
        clear();
        
        //when
        Comment findComment = commentRepository.findById(id).orElse(null);
        
        //then
        assertEquals("comment == findComment", comment, findComment);
    }
    
    @Test
    public void PK로검색_없는PK() {
        //given
        long id = new Random().nextLong();
        
        //when
        Optional<Comment> findComment = commentRepository.findById(id);
        
        //then
        assertFalse("findComment is empty", findComment.isPresent());
    }
    
    @Test
    public void 수정() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        em.persist(writer);
        em.persist(category);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        
        Comment comment = createComment(writer, post, "댓글 내용");
        em.persist(comment);
        Long id = comment.getId();
        clear();
        
        //when
        Comment findComment = commentRepository.findById(id).orElse(null);
        
        findComment.updateContent("댓글 내용 수정");
        
        clear();
        
        //then
        Comment updatedComment = em.find(Comment.class, id);
        
        assertNotEquals("comment != updatedComment", comment, updatedComment);
        assertEquals("Content must be changed", "댓글 내용 수정", updatedComment.getContent());
    }
    
    @Test
    public void 삭제() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        em.persist(writer);
        em.persist(category);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        
        Comment comment = createComment(writer, post, "댓글 내용");
        em.persist(comment);
        Long id = comment.getId();
        clear();
        
        //when
        Comment findComment = em.find(Comment.class, id);
        commentRepository.delete(findComment);
        clear();
        
        //then
        assertFalse("Comment must not be found", commentRepository.findById(id).isPresent());
    }
    
    @Test
    public void findAllByPostOrderByCreatedDateDesc() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        Category category = createCategory("분류");
        categories.add(category);
        em.persist(category);
        
        Post post = createPost(writers.get(new Random().nextInt(writers.size() - 1)),
                               "게시물 제목",
                               "게시글 내용",
                               category,
                               false);
        em.persist(post);
        
        clear();
        
        List<Comment> comments = createComments(writers, post, 100);
        
        //when
        List<Comment> content = commentRepository.findAllByPostOrderByCreatedDateDesc(post, PageRequest.of(0, 10))
                                                 .getContent();
        
        //then
        assertTrue("comments should include content", comments.containsAll(content));
        assertEquals("The size of content must be 10", 10, content.size());
    }
    
    @Test
    public void 기간및갯수로목록조회() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        Category category = createCategory("분류");
        categories.add(category);
        em.persist(category);
        
        Post post = createPost(writers.get(new Random().nextInt(writers.size() - 1)),
                               "게시물 제목",
                               "게시글 내용",
                               category,
                               false);
        em.persist(post);
        
        clear();
        
        List<Comment> comments = createComments(writers, post, 100);
        
        //when
        List<Comment> content = commentRepository.findTopNByPeriod(5, "1d");
        
        //then
        assertTrue("comments should include content", comments.containsAll(content));
        assertEquals("The size of content must be 5", 5, content.size());
    }
    
    @Test
    public void 기간및갯수로목록조회_작성자추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        Category category = createCategory("분류");
        categories.add(category);
        em.persist(category);
        
        Post post = createPost(writers.get(new Random().nextInt(writers.size() - 1)),
                               "게시물 제목",
                               "게시글 내용",
                               category,
                               false);
        em.persist(post);
        
        clear();
        
        List<Comment> comments = createComments(writers, post, 100);
        
        Member writer = writers.get(new Random().nextInt(writers.size() - 1));
        
        //when
        List<Comment> content = commentRepository.findTopNByPeriod(writer, 5, "1d");
        
        //then
        List<Comment> eq = comments.stream().filter(comment -> comment.getWriter() == writer).collect(toList());
        List<Comment> ne = comments.stream().filter(comment -> comment.getWriter() != writer).collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 5", content.size() <= 5);
        if (!content.isEmpty()) assertFalse("content should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간으로목록조회() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        Category category = createCategory("분류");
        categories.add(category);
        em.persist(category);
        
        Post post = createPost(writers.get(new Random().nextInt(writers.size() - 1)),
                               "게시물 제목",
                               "게시글 내용",
                               category,
                               false);
        em.persist(post);
        
        clear();
        
        List<Comment> comments = createComments(writers, post, 100);
        
        //when
        List<Comment> content = commentRepository.findByPeriod("1d", PageRequest.of(0, 10)).getContent();
        
        //then
        assertTrue("comments should include content", comments.containsAll(content));
        assertEquals("The size of content must be 10", 10, content.size());
    }
    
    @Test
    public void 기간으로목록조회_작성자추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        Category category = createCategory("분류");
        categories.add(category);
        em.persist(category);
        
        Post post = createPost(writers.get(new Random().nextInt(writers.size() - 1)),
                               "게시물 제목",
                               "게시글 내용",
                               category,
                               false);
        em.persist(post);
        
        clear();
        
        List<Comment> comments = createComments(writers, post, 100);
        
        Member writer = writers.get(new Random().nextInt(writers.size() - 1));
        
        //when
        List<Comment> content = commentRepository.findByPeriod(writer, "1d", PageRequest.of(0, 10)).getContent();
        
        //then
        List<Comment> eq = comments.stream().filter(comment -> comment.getWriter() == writer).collect(toList());
        List<Comment> ne = comments.stream().filter(comment -> comment.getWriter() != writer).collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("content should not include content", ne.containsAll(content));
    }
    
    public Comment createComment(Member writer, Post post, String content) {
        return Comment.builder().writer(writer).post(post).content(content).build();
    }
    
    private List<Comment> createComments(List<Member> writers, Post post, int count) {
        Random        random   = new Random();
        List<Comment> comments = new ArrayList<>();
        
        Member writer;
        
        for (int i = 1; i <= count; i++) {
            if (writers.size() > 1) writer = writers.get(random.nextInt(writers.size()));
            else if (writers.size() == 1) writer = writers.get(0);
            else return comments;
            
            Comment comment = createComment(writer, post, "댓글 내용 " + i);
            
            comments.add(comment);
            em.persist(comment);
        }
        clear();
        
        return comments;
    }
    
    private Post createPost(Member writer, String title, String content, Category category, boolean noticeYn) {
        return Post.builder()
                   .writer(writer)
                   .title(title)
                   .content(content)
                   .category(category)
                   .noticeYn(noticeYn)
                   .build();
    }
    
    private Member createMember(String username, String password, String authToken, String nickname) {
        return Member.builder()
                     .username(username)
                     .password(password)
                     .emailAuth(EmailAuth.builder().authToken(authToken).build())
                     .nickname(nickname)
                     .build();
    }
    
    private Category createCategory(String name) {
        return Category.builder().name(name).build();
    }
    
}