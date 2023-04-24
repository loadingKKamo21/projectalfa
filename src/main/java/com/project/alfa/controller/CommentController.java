package com.project.alfa.controller;

import com.project.alfa.common.auth.CustomUserDetails;
import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.common.error.exception.InvalidValueException;
import com.project.alfa.service.CommentService;
import com.project.alfa.service.dto.CommentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity commentsList(@RequestBody Map<String, Long> param, Pageable pageable) {
        return new ResponseEntity<>(commentService.readComments(param.get("id"), pageable), HttpStatus.OK);
    }
    
    @PostMapping(value = "/write", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity write(@RequestBody CommentRequestDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return new ResponseEntity<>("Invalid value", HttpStatus.BAD_REQUEST);
        if (commentService.save(dto) != null) return new ResponseEntity<>("Success", HttpStatus.OK);
        return new ResponseEntity<>("Invalid value", HttpStatus.BAD_REQUEST);
    }
    
    @PostMapping(value = "/update", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity update(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestBody CommentRequestDto dto,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return new ResponseEntity<>("Invalid value", HttpStatus.BAD_REQUEST);
        Long id = ((CustomUserDetails) userDetails).getId();
        if (id.equals(dto.getWId())) dto.setWId(id);
        
        commentService.update(dto);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
    
    @PostMapping(value = "/delete", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity delete(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CommentRequestDto dto) {
        Long id = ((CustomUserDetails) userDetails).getId();
        if (id.equals(dto.getWId())) dto.setWId(id);
        
        commentService.delete(dto.getWId(), dto.getId());
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
    
    @PostMapping(value = "/top10NewComments", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity top10NewComments() {
        return new ResponseEntity<>(commentService.findTopNNewComments(10), HttpStatus.OK);
    }
    
    @ExceptionHandler({ EntityNotFoundException.class, InvalidValueException.class })
    public ResponseEntity handleEntityNotFoundException() {
        return new ResponseEntity<>("Invalid value", HttpStatus.BAD_REQUEST);
    }
    
}
