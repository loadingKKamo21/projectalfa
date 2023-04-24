package com.project.alfa.service;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.domain.Category;
import com.project.alfa.domain.EmailAuth;
import com.project.alfa.domain.Member;
import com.project.alfa.domain.Post;
import com.project.alfa.repository.PostRepository;
import com.project.alfa.service.dto.PostListResponseDto;
import com.project.alfa.service.dto.PostReadResponseDto;
import com.project.alfa.service.dto.PostWriteRequestDto;
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
public class PostServiceTest {
    
    @Autowired
    PostService    postService;
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
    public void 게시글저장() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        
        em.persist(writer);
        em.persist(category);
        Long wId = writer.getId();
        Long cId = category.getId();
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(null, wId, cId, "게시글 제목", "게시글 내용", false);
        
        //when
        Long id = postService.save(dto);
        clear();
        
        //then
        Post findPost = em.find(Post.class, id);
        
        assertEquals("Title same", dto.getTitle(), findPost.getTitle());
        assertEquals("Content same", dto.getContent(), findPost.getContent());
        assertEquals("NoticeYn same", dto.getNoticeYn(), findPost.getNoticeYn());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글저장_계정엔티티조회불가() {
        //given
        Category category = createCategory("분류");
        
        em.persist(category);
        Long wId = new Random().nextLong();
        Long cId = category.getId();
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(null, wId, cId, "게시글 제목", "게시글 내용", false);
        
        //when
        postService.save(dto);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글저장_카테고리엔티티조회불가() {
        //given
        Member writer = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        
        em.persist(writer);
        Long wId = writer.getId();
        Long cId = new Random().nextLong();
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(null, wId, cId, "게시글 제목", "게시글 내용", false);
        
        //when
        postService.save(dto);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 게시글읽기() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        Post     post     = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        
        em.persist(writer);
        em.persist(category);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        String sessionId = UUID.randomUUID().toString();
        String ipAddress = UUID.randomUUID().toString();
        
        //when
        PostReadResponseDto dto = postService.readPost(id, sessionId, ipAddress);
        clear();
        
        //then
        Post findPost = em.find(Post.class, id);
        
        assertEquals("PK same", dto.getId(), findPost.getId());
        assertEquals("Writer PK same", dto.getWId(), findPost.getWriter().getId());
        assertEquals("Category PK same", dto.getCId(), findPost.getCategory().getId());
        assertEquals("Title same", dto.getTitle(), findPost.getTitle());
        assertEquals("Content same", dto.getContent(), findPost.getContent());
        assertEquals("NoticeYn same", dto.getNoticeYn(), findPost.getNoticeYn());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글읽기_게시글엔티티조회불가() {
        //given
        Long id = new Random().nextLong();
        
        String sessionId = UUID.randomUUID().toString();
        String ipAddress = UUID.randomUUID().toString();
        
        //when
        postService.readPost(id, sessionId, ipAddress);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 게시글읽기_OnlyPK() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        Post     post     = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        
        em.persist(writer);
        em.persist(category);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        //when
        PostReadResponseDto dto = postService.readPost(id);
        clear();
        
        //then
        Post findPost = em.find(Post.class, id);
        
        assertEquals("PK same", dto.getId(), findPost.getId());
        assertEquals("Writer PK same", dto.getWId(), findPost.getWriter().getId());
        assertEquals("Category PK same", dto.getCId(), findPost.getCategory().getId());
        assertEquals("Title same", dto.getTitle(), findPost.getTitle());
        assertEquals("Content same", dto.getContent(), findPost.getContent());
        assertEquals("NoticeYn same", dto.getNoticeYn(), findPost.getNoticeYn());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글읽기_OnlyPK_게시글엔티티조회불가() {
        //given
        Long id = new Random().nextLong();
        
        //when
        postService.readPost(id);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 조회수증가() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        Post     post     = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        
        em.persist(writer);
        em.persist(category);
        em.persist(post);
        Long id = post.getId();
        clear();
        
        int beforeViewCount = post.getViewCount();
        
        String sessionId = UUID.randomUUID().toString();
        String ipAddress = UUID.randomUUID().toString();
        
        //when
        postService.updateViewCount(id, sessionId, ipAddress);
        
        //then
        int afterViewCount = em.find(Post.class, id).getViewCount();
        
        assertNotEquals("ViewCount changed", beforeViewCount, afterViewCount);
        assertEquals("ViewCount changed", beforeViewCount + 1, afterViewCount);
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 조회수증가_게시글엔티티조회불가() {
        //given
        Long id = new Random().nextLong();
        
        String sessionId = UUID.randomUUID().toString();
        String ipAddress = UUID.randomUUID().toString();
        
        //when
        postService.updateViewCount(id, sessionId, ipAddress);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 게시글목록조회_모든카테고리() {
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
        List<PostListResponseDto> content = postService.findAll("ALL", false, PageRequest.of(0, 10)).getContent();
        
        //then
        List<PostListResponseDto> list = posts.stream().map(PostListResponseDto::new).collect(toList());
        
        for (int i = 0; i < content.size(); i++)
            for (int j = 0; j < list.size(); j++)
                if (list.get(j).getId() == content.get(i).getId()) {
                    assertEquals("PK same", list.get(j).getId(), content.get(i).getId());
                    assertEquals("Writer same", list.get(j).getWNickname(), content.get(i).getWNickname());
                    assertEquals("Title same", list.get(j).getTitle(), content.get(i).getTitle());
                    assertEquals("Category same", list.get(j).getCategory(), content.get(i).getCategory());
                    break;
                }
        assertEquals("The size of content must be 10", 10, content.size());
    }
    
    @Test
    public void 게시글목록조회_모든카테고리_공지여부추가() {
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
        List<PostListResponseDto> content = postService.findAll("ALL", true, PageRequest.of(0, 10)).getContent();
        
        //then
        List<PostListResponseDto> list = posts.stream()
                                              .filter(post -> post.getNoticeYn())
                                              .map(PostListResponseDto::new)
                                              .collect(toList());
        
        assertTrue("All noticeYn are true", content.stream().allMatch(dto -> dto.getNoticeYn()));
        for (int i = 0; i < content.size(); i++)
            for (int j = 0; j < list.size(); j++)
                if (list.get(j).getId() == content.get(i).getId()) {
                    assertEquals("PK same", list.get(j).getId(), content.get(i).getId());
                    assertEquals("Writer same", list.get(j).getWNickname(), content.get(i).getWNickname());
                    assertEquals("Title same", list.get(j).getTitle(), content.get(i).getTitle());
                    assertEquals("Category same", list.get(j).getCategory(), content.get(i).getCategory());
                    break;
                }
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
    }
    
    @Test
    public void 게시글목록조회_지정카테고리() {
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
        List<PostListResponseDto> content = postService.findAll(category.getName(), false, PageRequest.of(0, 10))
                                                       .getContent();
        
        //then
        List<PostListResponseDto> list = posts.stream()
                                              .filter(post -> post.getCategory() == category)
                                              .map(PostListResponseDto::new)
                                              .collect(toList());
        
        assertTrue("All categories are the same", content.stream().allMatch(dto -> dto.getCategory().equals("-")));
        for (int i = 0; i < content.size(); i++)
            for (int j = 0; j < list.size(); j++)
                if (list.get(j).getId() == content.get(i).getId()) {
                    assertEquals("PK same", list.get(j).getId(), content.get(i).getId());
                    assertEquals("Writer same", list.get(j).getWNickname(), content.get(i).getWNickname());
                    assertEquals("Title same", list.get(j).getTitle(), content.get(i).getTitle());
                    assertEquals("Category same", list.get(j).getCategory(), content.get(i).getCategory());
                    break;
                }
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
    }
    
    @Test
    public void 게시글목록조회_지정카테고리_공지여부추가() {
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
        List<PostListResponseDto> content = postService.findAll(category.getName(), true, PageRequest.of(0, 10))
                                                       .getContent();
        
        //then
        List<PostListResponseDto> list = posts.stream()
                                              .filter(post -> post.getCategory() == category)
                                              .map(PostListResponseDto::new)
                                              .collect(toList());
        
        assertTrue("All categories are the same, all noticeYn are true",
                   content.stream().allMatch(dto -> dto.getCategory().equals("-") && dto.getNoticeYn()));
        for (int i = 0; i < content.size(); i++)
            for (int j = 0; j < list.size(); j++)
                if (list.get(j).getId() == content.get(i).getId()) {
                    assertEquals("PK same", list.get(j).getId(), content.get(i).getId());
                    assertEquals("Writer same", list.get(j).getWNickname(), content.get(i).getWNickname());
                    assertEquals("Title same", list.get(j).getTitle(), content.get(i).getTitle());
                    assertEquals("Category same", list.get(j).getCategory(), content.get(i).getCategory());
                    break;
                }
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글목록조회_지정카테고리_카테고리엔티티조회불가() {
        //given
        String categoryName = UUID.randomUUID().toString();
        
        //when
        postService.findAll(categoryName, false, PageRequest.of(0, 10));
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 검색목록조회_모든카테고리() {
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
        List<PostListResponseDto> content = postService.findAllWithKeyword("ALL",
                                                                           condition,
                                                                           keyword,
                                                                           false,
                                                                           PageRequest.of(0, 10)).getContent();
        
        //then
        List<PostListResponseDto> eq = posts.stream()
                                            .filter(post -> post.getTitle().contains(keyword))
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        List<PostListResponseDto> ne = posts.stream()
                                            .filter(post -> !post.getTitle().contains(keyword))
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        
        assertTrue("All titles must contain keyword",
                   content.stream().allMatch(dto -> dto.getTitle().contains(keyword)));
        for (int i = 0; i < content.size(); i++) {
            for (int j = 0; j < eq.size(); j++)
                if (eq.get(j).getId() == content.get(i).getId()) {
                    assertEquals("PK same", eq.get(j).getId(), content.get(i).getId());
                    assertEquals("Writer same", eq.get(j).getWNickname(), content.get(i).getWNickname());
                    assertEquals("Title same", eq.get(j).getTitle(), content.get(i).getTitle());
                    assertEquals("Category same", eq.get(j).getCategory(), content.get(i).getCategory());
                    break;
                }
            for (int j = 0; j < ne.size(); j++)
                assertNotEquals("PK not same", ne.get(j).getId(), content.get(i).getId());
        }
        assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
    }
    
    @Test
    public void 검색목록조회_모든카테고리_공지여부추가() {
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
        
        String condition = "title";
        String keyword   = "1";
        
        //when
        List<PostListResponseDto> content = postService.findAllWithKeyword("ALL",
                                                                           condition,
                                                                           keyword,
                                                                           true,
                                                                           PageRequest.of(0, 10)).getContent();
        
        //then
        List<PostListResponseDto> eq = posts.stream()
                                            .filter(post -> post.getTitle().contains(keyword) && post.getNoticeYn())
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        List<PostListResponseDto> ne = posts.stream()
                                            .filter(post -> !post.getTitle().contains(keyword) || !post.getNoticeYn())
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        
        if (!content.isEmpty()) {
            assertTrue("All titles must contain keyword, all noticeYn are true",
                       content.stream().allMatch(dto -> dto.getTitle().contains(keyword) && dto.getNoticeYn()));
            for (int i = 0; i < content.size(); i++) {
                for (int j = 0; j < eq.size(); j++)
                    if (eq.get(j).getId() == content.get(i).getId()) {
                        assertEquals("PK same", eq.get(j).getId(), content.get(i).getId());
                        assertEquals("Writer same", eq.get(j).getWNickname(), content.get(i).getWNickname());
                        assertEquals("Title same", eq.get(j).getTitle(), content.get(i).getTitle());
                        assertEquals("Category same", eq.get(j).getCategory(), content.get(i).getCategory());
                        break;
                    }
                for (int j = 0; j < ne.size(); j++)
                    assertNotEquals("PK not same", ne.get(j).getId(), content.get(i).getId());
            }
            assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        }
    }
    
    @Test
    public void 검색목록조회_지정카테고리() {
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
        List<PostListResponseDto> content = postService.findAllWithKeyword(category.getName(),
                                                                           condition,
                                                                           keyword,
                                                                           false,
                                                                           PageRequest.of(0, 10)).getContent();
        
        //then
        List<PostListResponseDto> eq = posts.stream()
                                            .filter(post -> post.getCategory() == category && post.getTitle()
                                                                                                  .contains(keyword))
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        List<PostListResponseDto> ne = posts.stream()
                                            .filter(post -> post.getCategory() != category || !post.getTitle()
                                                                                                   .contains(keyword))
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        if (!content.isEmpty()) {
            assertTrue("All categories are the same, all titles must contain keyword",
                       content.stream()
                              .allMatch(dto -> dto.getCategory().equals("-") && dto.getTitle().contains(keyword)));
            for (int i = 0; i < content.size(); i++) {
                for (int j = 0; j < eq.size(); j++)
                    if (eq.get(j).getId() == content.get(i).getId()) {
                        assertEquals("PK same", eq.get(j).getId(), content.get(i).getId());
                        assertEquals("Writer same", eq.get(j).getWNickname(), content.get(i).getWNickname());
                        assertEquals("Title same", eq.get(j).getTitle(), content.get(i).getTitle());
                        assertEquals("Category same", eq.get(j).getCategory(), content.get(i).getCategory());
                        break;
                    }
                for (int j = 0; j < ne.size(); j++)
                    assertNotEquals("PK not same", ne.get(j).getId(), content.get(i).getId());
            }
            assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        }
    }
    
    @Test
    public void 검색목록조회_지정카테고리_공지여부추가() {
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
        
        Category category  = categories.get(new Random().nextInt(categories.size() - 1));
        String   condition = "title";
        String   keyword   = "1";
        
        //when
        List<PostListResponseDto> content = postService.findAllWithKeyword(category.getName(),
                                                                           condition,
                                                                           keyword,
                                                                           true,
                                                                           PageRequest.of(0, 10)).getContent();
        
        //then
        List<PostListResponseDto> eq = posts.stream()
                                            .filter(post -> post.getCategory() == category && post.getTitle()
                                                                                                  .contains(keyword) && post.getNoticeYn())
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        List<PostListResponseDto> ne = posts.stream()
                                            .filter(post -> post.getCategory() != category || !post.getTitle()
                                                                                                   .contains(keyword) || !post.getNoticeYn())
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        if (!content.isEmpty()) {
            assertTrue("All categories are the same, all titles must contain keyword, all noticeYn are true",
                       content.stream()
                              .allMatch(dto -> dto.getCategory().equals("-") && dto.getTitle()
                                                                                   .contains(keyword) && dto.getNoticeYn()));
            for (int i = 0; i < content.size(); i++) {
                for (int j = 0; j < eq.size(); j++)
                    if (eq.get(j).getId() == content.get(i).getId()) {
                        assertEquals("PK same", eq.get(j).getId(), content.get(i).getId());
                        assertEquals("Writer same", eq.get(j).getWNickname(), content.get(i).getWNickname());
                        assertEquals("Title same", eq.get(j).getTitle(), content.get(i).getTitle());
                        assertEquals("Category same", eq.get(j).getCategory(), content.get(i).getCategory());
                        break;
                    }
                for (int j = 0; j < ne.size(); j++)
                    assertNotEquals("PK not same", ne.get(j).getId(), content.get(i).getId());
            }
            assertTrue("The size of content must be less than or equal to 10", content.size() <= 10);
        }
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 검색목록조회_지정카테고리_카테고리엔티티조회불가() {
        //given
        String categoryName = UUID.randomUUID().toString();
        String condition    = "title";
        String keyword      = "10";
        
        //when
        postService.findAllWithKeyword(categoryName, condition, keyword, false, PageRequest.of(0, 10));
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 기간및갯수로목록조회_모든카테고리() {
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
        List<PostListResponseDto> content = postService.findTopNNotice("ALL", 3);
        
        //then
        List<PostListResponseDto> eq = posts.stream()
                                            .filter(post -> post.getNoticeYn())
                                            .sorted(Comparator.comparing(Post::getCreatedDate).reversed())
                                            .limit(3)
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        List<PostListResponseDto> ne = posts.stream()
                                            .filter(post -> !post.getNoticeYn())
                                            .sorted(Comparator.comparing(Post::getCreatedDate).reversed())
                                            .limit(3)
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        
        if (!content.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                assertEquals("PK same", eq.get(i).getId(), content.get(i).getId());
                assertEquals("Writer same", eq.get(i).getWNickname(), content.get(i).getWNickname());
                assertEquals("Title same", eq.get(i).getTitle(), content.get(i).getTitle());
                assertEquals("Category same", eq.get(i).getCategory(), content.get(i).getCategory());
                break;
            }
        }
        assertTrue("The size of content must be less than or equal to 3", content.size() <= 3);
    }
    
    @Test
    public void 기간및갯수로목록조회_지정카테고리() {
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
        List<PostListResponseDto> content = postService.findTopNNotice(category.getName(), 3);
        
        //then
        List<PostListResponseDto> eq = posts.stream()
                                            .filter(post -> post.getCategory() == category && post.getNoticeYn())
                                            .sorted(Comparator.comparing(Post::getCreatedDate).reversed())
                                            .limit(3)
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        List<PostListResponseDto> ne = posts.stream()
                                            .filter(post -> post.getCategory() != category || !post.getNoticeYn())
                                            .sorted(Comparator.comparing(Post::getCreatedDate).reversed())
                                            .limit(3)
                                            .map(PostListResponseDto::new)
                                            .collect(toList());
        
        assertTrue("All categories are the same, all noticeYn are true",
                   content.stream().allMatch(dto -> dto.getCategory().equals("-") && dto.getNoticeYn()));
        if (!content.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                assertEquals("PK same", eq.get(i).getId(), content.get(i).getId());
                assertEquals("Writer same", eq.get(i).getWNickname(), content.get(i).getWNickname());
                assertEquals("Title same", eq.get(i).getTitle(), content.get(i).getTitle());
                assertEquals("Category same", eq.get(i).getCategory(), content.get(i).getCategory());
                break;
            }
        }
        assertTrue("The size of content must be less than or equal to 3", content.size() <= 3);
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 기간및갯수로목록조회_지정카테고리_카테고리엔티티조회불가() {
        //given
        String categoryName = UUID.randomUUID().toString();
        
        //when
        postService.findTopNNotice(categoryName, 3);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 게시글수정() {
        //given
        Member   writer    = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category1 = createCategory("분류1");
        Category category2 = createCategory("분류2");
        
        em.persist(writer);
        em.persist(category1);
        em.persist(category2);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category1, false);
        em.persist(post);
        
        Long id  = post.getId();
        Long wId = writer.getId();
        Long cId = category2.getId();
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(id, wId, cId, "게시글 제목 수정", "게시글 내용 수정", true);
        
        //when
        postService.update(dto);
        clear();
        
        //then
        Post findPost = em.find(Post.class, id);
        
        assertEquals("Title must be changed", dto.getTitle(), findPost.getTitle());
        assertNotEquals("Title must not be same as before", post.getTitle(), findPost.getTitle());
        assertEquals("Content must be changed", dto.getContent(), findPost.getContent());
        assertNotEquals("Content must not be same as before", post.getContent(), findPost.getContent());
        assertEquals("NoticeYn must be changed", dto.getNoticeYn(), findPost.getNoticeYn());
        assertNotEquals("NoticeYn must not be same as before", post.getNoticeYn(), findPost.getNoticeYn());
        assertEquals("Category must be changed", dto.getCId(), findPost.getCategory().getId());
        assertNotEquals("Category must not be same as before",
                        post.getCategory().getId(),
                        findPost.getCategory().getId());
        assertEquals("Category must be changed", category2.getName(), findPost.getCategory().getName());
        assertNotEquals("Category must not be same as before",
                        post.getCategory().getName(),
                        findPost.getCategory().getName());
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글수정_게시글엔티티조회불가() {
        //given
        Member   writer    = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category1 = createCategory("분류1");
        Category category2 = createCategory("분류2");
        
        em.persist(writer);
        em.persist(category1);
        em.persist(category2);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category1, false);
        em.persist(post);
        
        Long id;
        do {
            id = new Random().nextLong();
        } while (id.equals(post.getId()));
        Long wId = writer.getId();
        Long cId = category2.getId();
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(id, wId, cId, "게시글 제목 수정", "게시글 내용 수정", true);
        
        //when
        postService.update(dto);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글수정_계정엔티티조회불가() {
        //given
        Member   writer    = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category1 = createCategory("분류1");
        Category category2 = createCategory("분류2");
        
        em.persist(writer);
        em.persist(category1);
        em.persist(category2);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category1, false);
        em.persist(post);
        
        Long id = post.getId();
        Long wId;
        do {
            wId = new Random().nextLong();
        } while (wId.equals(writer.getId()));
        Long cId = category2.getId();
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(id, wId, cId, "게시글 제목 수정", "게시글 내용 수정", true);
        
        //when
        postService.update(dto);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 게시글수정_작성자불일치() {
        //given
        Member   writer1   = createMember("user1@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자1");
        Member   writer2   = createMember("user2@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자2");
        Category category1 = createCategory("분류1");
        Category category2 = createCategory("분류2");
        
        em.persist(writer1);
        em.persist(writer2);
        em.persist(category1);
        em.persist(category2);
        
        Post post = createPost(writer1, "게시글 제목", "게시글 내용", category1, false);
        em.persist(post);
        
        Long id  = post.getId();
        Long wId = writer2.getId();
        Long cId = category2.getId();
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(id, wId, cId, "게시글 제목 수정", "게시글 내용 수정", true);
        
        //when
        postService.update(dto);
        
        //then
        fail("InvalidValueException");
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글수정_카테고리엔티티조회불가() {
        //given
        Member   writer    = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category1 = createCategory("분류1");
        Category category2 = createCategory("분류2");
        
        em.persist(writer);
        em.persist(category1);
        em.persist(category2);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category1, false);
        em.persist(post);
        
        Long id  = post.getId();
        Long wId = writer.getId();
        Long cId;
        do {
            cId = new Random().nextLong();
        } while (cId.equals(category1.getId()) || cId.equals(category2.getId()));
        clear();
        
        PostWriteRequestDto dto = createWriteRequestDto(id, wId, cId, "게시글 제목 수정", "게시글 내용 수정", true);
        
        //when
        postService.update(dto);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test
    public void 게시글삭제() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        
        em.persist(writer);
        em.persist(category);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        
        Long id       = post.getId();
        Long writerId = writer.getId();
        clear();
        
        //when
        postService.delete(writerId, id);
        clear();
        
        //then
        assertTrue("Post must not be found", em.find(Post.class, id) == null);
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글삭제_게시글엔티티조회불가() {
        //given
        Member writer = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        
        em.persist(writer);
        
        Long id       = new Random().nextLong();
        Long writerId = writer.getId();
        clear();
        
        //when
        postService.delete(writerId, id);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void 게시글삭제_계정엔티티조회불가() {
        //given
        Member   writer   = createMember("user@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자");
        Category category = createCategory("분류");
        
        em.persist(writer);
        em.persist(category);
        
        Post post = createPost(writer, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        
        Long id = post.getId();
        Long writerId;
        do {
            writerId = new Random().nextLong();
        } while (writerId.equals(writer.getId()));
        clear();
        
        //when
        postService.delete(writerId, id);
        
        //then
        fail("EntityNotFoundException");
    }
    
    @Test(expected = InvalidValueException.class)
    public void 게시글삭제_작성자불일치() {
        //given
        Member   writer1  = createMember("user1@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자1");
        Member   writer2  = createMember("user2@mail.com", "User12!@", UUID.randomUUID().toString(), "사용자2");
        Category category = createCategory("분류");
        
        em.persist(writer1);
        em.persist(writer2);
        em.persist(category);
        
        Post post = createPost(writer1, "게시글 제목", "게시글 내용", category, false);
        em.persist(post);
        
        Long id       = post.getId();
        Long writerId = writer2.getId();
        clear();
        
        //when
        postService.delete(writerId, id);
        
        //then
        fail("InvalidValueException");
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
    
    private PostWriteRequestDto createWriteRequestDto(Long id,
                                                      Long wId,
                                                      Long cId,
                                                      String title,
                                                      String content,
                                                      Boolean noticeYn) {
        PostWriteRequestDto dto = new PostWriteRequestDto();
        dto.setId(id);
        dto.setWId(wId);
        dto.setCId(cId);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setNoticeYn(noticeYn);
        return dto;
    }
    
}