package com.project.alfa.service;

import com.project.alfa.common.error.exception.EntityNotFoundException;
import com.project.alfa.repository.CategoryRepository;
import com.project.alfa.service.dto.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * 카테고리 조회
     *
     * @param name - 카테고리 명
     * @return 카테고리 정보
     */
    public CategoryResponseDto findByName(final String name) {
        return new CategoryResponseDto(categoryRepository.findByName(name)
                                                         .orElseThrow(() -> new EntityNotFoundException(
                                                                 "Could not found 'Category' entity by name: " + name)));
    }
    
    /**
     * 카테고리 목록 조회
     *
     * @return 카테고리 목록
     */
    public List<CategoryResponseDto> findAllCategories() {
        return categoryRepository.findAll().stream().map(CategoryResponseDto::new).collect(toList());
    }
    
}
