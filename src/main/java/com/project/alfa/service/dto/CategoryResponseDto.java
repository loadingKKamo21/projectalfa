package com.project.alfa.service.dto;

import com.project.alfa.domain.Category;
import lombok.Getter;

@Getter
public class CategoryResponseDto {
    
    private final Long   id;
    private final String name;
    private final String displayName;
    
    public CategoryResponseDto(Category category) {
        id = category.getId();
        name = category.getName();
        switch (category.getName()) {
            case "ALL":
                displayName = "-";
                break;
            case "FREE":
                displayName = "자유";
                break;
            case "QNA":
                displayName = "질문";
                break;
            default:
                displayName = "-";
                break;
        }
    }
    
}
