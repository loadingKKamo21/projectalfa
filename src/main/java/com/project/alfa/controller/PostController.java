package com.project.alfa.controller;

import com.project.alfa.common.auth.CustomUserDetails;
import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.domain.Role;
import com.project.alfa.service.CategoryService;
import com.project.alfa.service.PostService;
import com.project.alfa.service.dto.CategoryResponseDto;
import com.project.alfa.service.dto.PostReadResponseDto;
import com.project.alfa.service.dto.PostWriteRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService     postService;
    private final CategoryService categoryService;
    
    @GetMapping
    public String postsList(@RequestParam(required = false, defaultValue = "ALL") String category,
                            @RequestParam(required = false) String condition,
                            @RequestParam(required = false) String keyword,
                            Pageable pageable,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Boolean noticeYn = false;   //공지글 여부
        model.addAttribute("category", category);
        
        try {
            model.addAttribute("notices", postService.findTopNNotice(category, 3));    //게시판 분류에 따른 최근 공지 글 3개 포함
            
            if (keyword == null)    //검색하지 않은 경우
                model.addAttribute("posts", postService.findAll(category, noticeYn, pageable));
            else {                  //검색한 경우
                model.addAttribute("condition", condition);
                model.addAttribute("keyword", keyword);
                model.addAttribute("posts",
                                   postService.findAllWithKeyword(category, condition, keyword, noticeYn, pageable));
            }
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "존재하지 않는 게시판입니다.");
            return "redirect:/posts";
        }
        
        return "posts/list";
    }
    
    @GetMapping("/notice")
    public String noticePostsList(@RequestParam(defaultValue = "ALL") String category,
                                  @RequestParam(required = false) String condition,
                                  @RequestParam(required = false) String keyword,
                                  Pageable pageable,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Boolean noticeYn = true;   //공지글 여부
        model.addAttribute("category", category);
        
        try {
            if (keyword == null) model.addAttribute("posts", postService.findAll(category, noticeYn, pageable));
            else {
                model.addAttribute("condition", condition);
                model.addAttribute("keyword", keyword);
                model.addAttribute("posts",
                                   postService.findAllWithKeyword(category, condition, keyword, noticeYn, pageable));
            }
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "존재하지 않는 게시판입니다.");
            return "redirect:/posts";
        }
        
        return "posts/notice";
    }
    
    @GetMapping("/write")
    public String writeForm(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false, name = "notice") Boolean noticeYn,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        PostWriteRequestDto       form       = new PostWriteRequestDto();
        List<CategoryResponseDto> categories = categoryService.findAllCategories();
        
        if (category != null) for (CategoryResponseDto response : categories)
            if (response.getName().equals(category)) {
                form.setCId(response.getId());
                break;
            }
        
        if (noticeYn != null) {
            Boolean flag = false;
            for (GrantedAuthority authority : userDetails.getAuthorities())
                if (authority.getAuthority().equals(Role.ADMIN.value())) {
                    flag = true;
                    break;
                }
            if (noticeYn && !flag) {
                redirectAttributes.addFlashAttribute("message", "권한이 없는 계정입니다.\n관리자에게 문의하세요.");
                return "redirect:/posts";
            } else form.setNoticeYn(true);
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("form", form);
        
        return "posts/write";
    }
    
    @PostMapping("/write")
    public String write(@Valid @ModelAttribute("form") final PostWriteRequestDto form,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "posts/write";
        form.setWId(((CustomUserDetails) userDetails).getId());
        if (form.getNoticeYn() == null) form.setNoticeYn(false);
        
        Long postId = null;
        try {
            postId = postService.save(form);
        } catch (EntityNotFoundException e) {
            if (e.getMessage().contains("'Member' entity"))
                redirectAttributes.addFlashAttribute("message", "존재하지 않는 계정입니다.");
            if (e.getMessage().contains("'Category' entity"))
                redirectAttributes.addFlashAttribute("message", "존재하지 않는 카테고리입니다.");
            return "redirect:/posts";
        }
        
        return "redirect:/posts/read?id=" + postId;
    }
    
    @GetMapping("/read")
    public String read(@RequestParam Long id, HttpServletRequest request,
                       //                       HttpServletResponse response,
                       Model model, RedirectAttributes redirectAttributes) {
        /* 1. 조회 수 중복 증가 방지: Cookie */
        //        Cookie    oldCookie = null;
        //        Cookie[]  cookies   = request.getCookies();
        //        final int MAX_AGE   = 60 * 60 * 24;
        //
        //        if (cookies != null)
        //            for (Cookie cookie : cookies)
        //                if (cookie.getName().equals("readPost"))
        //                    oldCookie = cookie;
        //
        //        try {
        //            if (oldCookie != null) {
        //                if (!oldCookie.getValue().contains("[" + id + "]")) {
        //                    postService.updateViewCount(id);
        //
        //                    oldCookie.setValue(oldCookie.getValue() + "[" + id + "]");
        //                    oldCookie.setPath("/posts");
        //                    oldCookie.setMaxAge(MAX_AGE);
        //
        //                    response.addCookie(oldCookie);
        //                }
        //            } else {
        //                postService.updateViewCount(id);
        //
        //                Cookie newCookie = new Cookie("readPost", "[" + id + "]");
        //                newCookie.setPath("/posts");
        //                newCookie.setMaxAge(MAX_AGE);
        //
        //                response.addCookie(newCookie);
        //            }
        //
        //            model.addAttribute("post", postService.readPost(id));
        //        } catch (EntityNotFoundException e) {
        //            redirectAttributes.addFlashAttribute("message", "존재하지 않는 게시글입니다.");
        //            return "redirect:/posts";
        //        }
        
        /* 2. 조회 수 중복 증가 방지: Redis cache */
        try {
            postService.updateViewCount(id, request.getSession().getId(), request.getRemoteAddr());
            model.addAttribute("post", postService.readPost(id, request.getSession().getId(), request.getRemoteAddr()));
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "존재하지 않는 게시글입니다.");
            return "redirect:/posts";
        }
        
        return "posts/read";
    }
    
    @GetMapping("/update")
    public String updateForm(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam Long id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Long                wId  = ((CustomUserDetails) userDetails).getId();
        PostReadResponseDto post = null;
        
        try {
            post = postService.readPost(id);
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "존재하지 않는 게시글입니다.");
            return "redirect:/posts";
        }
        
        if (!wId.equals(post.getWId())) {
            redirectAttributes.addFlashAttribute("message", "권한이 없는 계정입니다.\n관리자에게 문의하세요.");
            return "redirect:/posts";
        }
        
        List<CategoryResponseDto> categories = categoryService.findAllCategories();
        
        PostWriteRequestDto form = new PostWriteRequestDto();
        form.setId(id);
        form.setWId(wId);
        form.setCId(post.getCId());
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());
        form.setNoticeYn(post.getNoticeYn());
        
        model.addAttribute("categories", categories);
        model.addAttribute("form", form);
        
        return "posts/update";
    }
    
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("form") PostWriteRequestDto form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long id = ((CustomUserDetails) userDetails).getId();
        if (id.equals(form.getWId())) form.setWId(id);
        
        try {
            postService.update(form);
        } catch (EntityNotFoundException e) {
            if (e.getMessage().contains("'Post' entity")) {
                redirectAttributes.addFlashAttribute("message", "");
                return "redirect:/posts";
            }
            if (e.getMessage().contains("'Category' entity"))
                redirectAttributes.addFlashAttribute("message", "존재하지 않는 카테고리입니다.");
        } catch (InvalidValueException e) {
            redirectAttributes.addFlashAttribute("message", "권한이 없는 계정입니다.\n관리자에게 문의하세요.");
        }
        
        return "redirect:/posts/read?id=" + form.getId();
    }
    
    @PostMapping("/delete")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            postService.delete(((CustomUserDetails) userDetails).getId(), id);
            redirectAttributes.addFlashAttribute("message", "게시글 삭제가 완료되었습니다.");
        } catch (EntityNotFoundException e) {
            if (e.getMessage().contains("'Post' entity"))
                redirectAttributes.addFlashAttribute("message", "존재하지 않는 게시글입니다.");
            if (e.getMessage().contains("'Member' entity"))
                redirectAttributes.addFlashAttribute("message", "존재하지 않는 계정입니다.");
        } catch (InvalidValueException e) {
            redirectAttributes.addFlashAttribute("message", "권한이 없는 계정입니다.\n관리자에게 문의하세요.");
        }
        
        return "redirect:/posts";
    }
    
    @ResponseBody
    @PostMapping(value = "/top10Notice", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity top10Notice() {
        return new ResponseEntity<>(postService.findTopNNotice("ALL", 10), HttpStatus.OK);
    }
    
    @ResponseBody
    @PostMapping(value = "/top10ViewCount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity top10ViewCount() {
        return new ResponseEntity<>(postService.findTopNViewCountToday(10), HttpStatus.OK);
    }
    
    @ResponseBody
    @PostMapping(value = "/top10CommentCount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity top10CommentCount() {
        return new ResponseEntity<>(postService.findTopNCommentCountToday(10), HttpStatus.OK);
    }
    
    @ResponseBody
    @PostMapping(value = "/top10NewPosts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity top10NewPosts() {
        return new ResponseEntity<>(postService.findTopNNewPosts(10), HttpStatus.OK);
    }
    
}
