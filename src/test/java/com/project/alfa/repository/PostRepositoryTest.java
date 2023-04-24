package com.project.alfa.repository;

import com.project.alfa.domain.Category;
import com.project.alfa.domain.EmailAuth;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.Post;
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
public class PostRepositoryTest {
    
    @Autowired
    PostRepository postRepository;
    @PersistenceContext
    EntityManager  em;
    
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
        clear();
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        
        //when
        postRepository.save(post);
        Long id = post.getId();
        clear();
        
        //then
        Post findPost = em.find(Post.class, id);
        
        assertEquals("post == findPost", post, findPost);
    }
    
    @Test
    public void PK로검색() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        
        em.persist(writer);
        em.persist(category);
        clear();
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        //when
        Post findPost = postRepository.findById(id).orElse(null);
        
        //then
        assertEquals("post == findPost", post, findPost);
    }
    
    @Test
    public void PK로검색_없는PK() {
        //given
        long id = new Random().nextLong();
        
        //when
        Optional<Post> findPost = postRepository.findById(id);
        
        //then
        assertFalse("findPost is empty", findPost.isPresent());
    }
    
    @Test
    public void PK존재여부() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        
        em.persist(writer);
        em.persist(category);
        clear();
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        //when
        boolean exists = postRepository.existsById(id);
        
        //then
        assertTrue("post exist", exists);
    }
    
    @Test
    public void PK존재여부_없는PK() {
        //given
        long id = new Random().nextLong();
        
        //when
        boolean exists = postRepository.existsById(id);
        
        //then
        assertFalse("post does not exist", exists);
    }
    
    @Test
    public void 수정() {
        //given
        Member   writer    = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category1 = createCategory("분류1");
        Category category2 = createCategory("분류2");
        
        em.persist(writer);
        em.persist(category1);
        em.persist(category2);
        clear();
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category1, false);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        //when
        Post findPost = postRepository.findById(id).orElse(null);
        
        findPost.updateTitle("게시글 제목 수정");
        findPost.updateContent("게시글 내용 수정");
        findPost.updateCategory(category2);
        findPost.updateNoticeYn(true);
        
        clear();
        
        //then
        Post updatedPost = em.find(Post.class, id);
        
        assertNotEquals("post != updatedPost", post, updatedPost);
        assertEquals("Title must be changed", "게시글 제목 수정", updatedPost.getTitle());
        assertNotEquals("Title must not be same as before", post.getTitle(), updatedPost.getTitle());
        assertEquals("Content must be changed", "게시글 내용 수정", updatedPost.getContent());
        assertNotEquals("Content must not be same as before", post.getContent(), updatedPost.getContent());
        assertEquals("Category must be changed", category2, updatedPost.getCategory());
        assertNotEquals("Category must not be same as before", post.getCategory(), updatedPost.getCategory());
        assertEquals("NoticeYn must be changed", true, updatedPost.getNoticeYn());
        assertNotEquals("NoticeYn must not be same as before", post.getNoticeYn(), updatedPost.getNoticeYn());
    }
    
    @Test
    public void 삭제() {
        //given
        Member   writer    = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category1 = createCategory("분류1");
        Category category2 = createCategory("분류2");
        
        em.persist(writer);
        em.persist(category1);
        em.persist(category2);
        clear();
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category1, false);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        //when
        Post findPost = em.find(Post.class, id);
        postRepository.delete(findPost);
        clear();
        
        //then
        assertFalse("Post must not be found", postRepository.findById(id).isPresent());
    }
    
    @Test
    public void findAllByNoticeYnOrderByCreatedDateDesc() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        //when
        List<Post> content = postRepository.findAllByNoticeYnOrderByCreatedDateDesc(false, PageRequest.of(0, 10))
                                           .getContent();
        
        //then
        assertTrue("posts should include content", posts.containsAll(content));
        assertEquals("The size of content must be 10", 10, content.size());
    }
    
    @Test
    public void findAllByCategoryAndNoticeYnOrderByCreatedDateDesc() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        Category category = categories.get(new Random().nextInt(categories.size() - 1));
        
        //when
        List<Post> content = postRepository.findAllByCategoryAndNoticeYnOrderByCreatedDateDesc(category,
                                                                                               false,
                                                                                               PageRequest.of(0, 10))
                                           .getContent();
        
        //then
        List<Post> eqCategory = posts.stream().filter(post -> post.getCategory() == category).collect(toList());
        List<Post> neCategory = posts.stream().filter(post -> post.getCategory() != category).collect(toList());
        
        assertTrue("eqCategory should include content", eqCategory.containsAll(content));
        assertFalse("If category is not equal, content is not included in neCategory", neCategory.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
    }
    
    @Test
    public void 키워드로검색목록조회() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        String condition = "title";
        String keyword   = "10";
        
        //when
        List<Post> content = postRepository.searchByKeyword(condition, keyword, false, PageRequest.of(0, 10))
                                           .getContent();
        
        //then
        List<Post> eq = posts.stream().filter(post -> post.getTitle().contains(keyword)).collect(toList());
        List<Post> ne = posts.stream().filter(post -> !post.getTitle().contains(keyword)).collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertFalse("If keyword is not in the title, content is not included in ne", ne.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
    }
    
    @Test
    public void 키워드로검색목록조회_카테고리추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        Category category  = categories.get(new Random().nextInt(categories.size() - 1));
        String   condition = "title";
        String   keyword   = "10";
        
        //when
        List<Post> content = postRepository.searchByKeyword(category, condition, keyword, false, PageRequest.of(0, 10))
                                           .getContent();
        
        //then
        List<Post> eq = posts.stream()
                             .filter(post -> post.getTitle().contains(keyword) && post.getCategory() == category)
                             .collect(toList());
        List<Post> ne = posts.stream()
                             .filter(post -> !post.getTitle().contains(keyword) || post.getCategory() != category)
                             .collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        if (!content.isEmpty()) assertFalse(
                "If keyword is not in the title and category is not equal, content is not included in ne",
                ne.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
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
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        //when
        List<Post> content = postRepository.findTopNByPeriod(10, "1d");
        
        //then
        assertTrue("posts should include content", posts.containsAll(content));
        assertEquals("The size of content must be 10", 10, content.size());
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
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        Member writer = writers.get(new Random().nextInt(writers.size() - 1));
        
        //when
        List<Post> content = postRepository.findTopNByPeriod(writer, 10, "1d");
        
        //then
        List<Post> eq = posts.stream().filter(post -> post.getWriter() == writer).collect(toList());
        List<Post> ne = posts.stream().filter(post -> post.getWriter() != writer).collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("content should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간및갯수로목록조회_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        //when
        List<Post> content = postRepository.findTopNByPeriod(10, true, "1d");
        
        //then
        List<Post> eq = posts.stream().filter(post -> post.getNoticeYn()).collect(toList());
        List<Post> ne = posts.stream().filter(post -> !post.getNoticeYn()).collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("content should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간및갯수로목록조회_작성자_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        Member writer = writers.get(new Random().nextInt(writers.size() - 1));
        
        //when
        List<Post> content = postRepository.findTopNByPeriod(writer, 10, true, "1d");
        
        //then
        List<Post> eq = posts.stream()
                             .filter(post -> post.getWriter() == writer && post.getNoticeYn())
                             .collect(toList());
        List<Post> ne = posts.stream()
                             .filter(post -> post.getWriter() != writer || !post.getNoticeYn())
                             .collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("content should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간및갯수로목록조회_카테고리_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        Category category = categories.get(new Random().nextInt(categories.size() - 1));
        
        //when
        List<Post> content = postRepository.findTopNByPeriod(category, 10, true, "1d");
        
        //then
        List<Post> eq = posts.stream()
                             .filter(post -> post.getCategory() == category && post.getNoticeYn())
                             .collect(toList());
        List<Post> ne = posts.stream()
                             .filter(post -> post.getCategory() != category || !post.getNoticeYn())
                             .collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("content should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간및갯수로목록조회_작성자_카테고리_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        Member   writer   = writers.get(new Random().nextInt(writers.size() - 1));
        Category category = categories.get(new Random().nextInt(categories.size() - 1));
        
        //when
        List<Post> content = postRepository.findTopNByPeriod(writer, category, 10, true, "1d");
        
        //then
        List<Post> eq = posts.stream()
                             .filter(post -> post.getWriter() == writer && post.getCategory() == category && post.getNoticeYn())
                             .collect(toList());
        List<Post> ne = posts.stream()
                             .filter(post -> post.getWriter() != writer || post.getCategory() != category || !post.getNoticeYn())
                             .collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
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
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        //when
        List<Post> content = postRepository.findByPeriod("1d", PageRequest.of(0, 10)).getContent();
        
        //then
        assertTrue("posts should include content", posts.containsAll(content));
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
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, false);
        
        Member writer = writers.get(new Random().nextInt(writers.size() - 1));
        
        //when
        List<Post> content = postRepository.findByPeriod(writer, "1d", PageRequest.of(0, 10)).getContent();
        
        //then
        List<Post> eq = posts.stream().filter(post -> post.getWriter() == writer).collect(toList());
        List<Post> ne = posts.stream().filter(post -> post.getWriter() != writer).collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("ne should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간으로목록조회_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        //when
        List<Post> content = postRepository.findByPeriod(true, "1d", PageRequest.of(0, 10)).getContent();
        
        //then
        List<Post> eq = posts.stream().filter(post -> post.getNoticeYn()).collect(toList());
        List<Post> ne = posts.stream().filter(post -> !post.getNoticeYn()).collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("ne should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간으로목록조회_작성자_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        Member writer = writers.get(new Random().nextInt(writers.size() - 1));
        
        //when
        List<Post> content = postRepository.findByPeriod(writer, true, "1d", PageRequest.of(0, 10)).getContent();
        
        //then
        List<Post> eq = posts.stream()
                             .filter(post -> post.getWriter() == writer && post.getNoticeYn())
                             .collect(toList());
        List<Post> ne = posts.stream()
                             .filter(post -> post.getWriter() != writer || !post.getNoticeYn())
                             .collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("ne should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간으로목록조회_카테고리_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        Category category = categories.get(new Random().nextInt(categories.size() - 1));
        
        //when
        List<Post> content = postRepository.findByPeriod(category, true, "1d", PageRequest.of(0, 10)).getContent();
        
        //then
        List<Post> eq = posts.stream()
                             .filter(post -> post.getCategory() == category && post.getNoticeYn())
                             .collect(toList());
        List<Post> ne = posts.stream()
                             .filter(post -> post.getCategory() != category || !post.getNoticeYn())
                             .collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("ne should not include content", ne.containsAll(content));
    }
    
    @Test
    public void 기간으로목록조회_작성자_카테고리_공지여부추가() {
        //given
        List<Member>   writers    = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Member writer = createMember("user" + i + "@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자" + i);
            writers.add(writer);
            em.persist(writer);
        }
        
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("분류" + i);
            categories.add(category);
            em.persist(category);
        }
        
        clear();
        
        List<Post> posts = createPosts(writers, categories, 100, true);
        
        Member   writer   = writers.get(new Random().nextInt(writers.size() - 1));
        Category category = categories.get(new Random().nextInt(categories.size() - 1));
        
        //when
        List<Post> content = postRepository.findByPeriod(writer, category, true, "1d", PageRequest.of(0, 10))
                                           .getContent();
        
        //then
        List<Post> eq = posts.stream()
                             .filter(post -> post.getWriter() == writer && post.getCategory() == category && post.getNoticeYn())
                             .collect(toList());
        List<Post> ne = posts.stream()
                             .filter(post -> post.getWriter() != writer || post.getCategory() != category || !post.getNoticeYn())
                             .collect(toList());
        
        assertTrue("eq should include content", eq.containsAll(content));
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        if (!content.isEmpty()) assertFalse("ne should not include content", ne.containsAll(content));
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
    
    private List<Post> createPosts(List<Member> writers,
                                   List<Category> categories,
                                   int count,
                                   boolean makeNoticeRandomly) {
        Random     random = new Random();
        List<Post> posts  = new ArrayList<>();
        
        Member   writer;
        Category category;
        Post     post;
        int      num = 0;
        
        if (makeNoticeRandomly) num = random.nextInt(count) + 1;
        
        for (int i = 1; i <= count; i++) {
            if (writers.size() > 1) writer = writers.get(random.nextInt(writers.size()));
            else if (writers.size() == 1) writer = writers.get(0);
            else return posts;
            
            if (categories.size() > 1) category = categories.get(random.nextInt(categories.size()));
            else if (categories.size() == 1) category = categories.get(0);
            else return posts;
            
            if (makeNoticeRandomly)
                if (i % num == 0) post = createPost(writer, "게시글 제목 " + i, "게시글 내용 " + i, category, true);
                else post = createPost(writer, "게시글 제목 " + i, "게시글 내용 " + i, category, false);
            else post = createPost(writer, "게시글 제목 " + i, "게시글 내용 " + i, category, false);
            
            posts.add(post);
            em.persist(post);
        }
        clear();
        
        return posts;
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