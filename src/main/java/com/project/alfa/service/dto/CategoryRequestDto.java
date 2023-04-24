package com.project.alfa.service.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CategoryRequestDto {
    
    private Long parentId;
    
    @NotBlank
    private String name;
    
}
