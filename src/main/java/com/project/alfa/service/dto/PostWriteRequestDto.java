package com.project.alfa.service.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PostWriteRequestDto {
    
    private Long id;
    private Long wId;
    private Long cId;
    
    @NotBlank(message = "제목을 입력하세요.")
    private String title;
    
    @NotBlank(message = "내용을 입력하세요.")
    private String content;
    
    private Boolean noticeYn;
    
}
