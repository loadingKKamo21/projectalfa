package com.project.alfa.repository;

import com.project.alfa.domain.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CategoryRepositoryTest {
    
    @Autowired
    CategoryRepository categoryRepository;
    @PersistenceContext
    EntityManager      em;
    
    @After
    public void clear() {
        em.flush();
        em.clear();
    }
    
    @Test
    public void 저장() {
        //given
        Category category = createCategory("분류");
        
        //when
        categoryRepository.save(category);
        Long id = category.getId();
        clear();
        
        //then
        Category findCategory = em.find(Category.class, id);
        
        assertEquals("category == findCategory", category, findCategory);
    }
    
    @Test
    public void PK로검색() {
        //given
        Category category = createCategory("분류");
        em.persist(category);
        Long id = category.getId();
        clear();
        
        //when
        Category findCategory = categoryRepository.findById(id).orElse(null);
        
        //then
        assertEquals("category == findCategory", category, findCategory);
    }
    
    @Test
    public void PK로검색_없는PK() {
        //given
        long id = new Random().nextLong();
        
        //when
        Optional<Category> findCategory = categoryRepository.findById(id);
        
        //then
        assertFalse("findCategory is empty", findCategory.isPresent());
    }
    
    @Test
    public void 수정() {
        //given
        Category category = createCategory("분류1");
        em.persist(category);
        Long id = category.getId();
        clear();
        
        //when
        Category findCategory = categoryRepository.findById(id).orElse(null);
        
        findCategory.updateCategoryName("분류2");
        
        clear();
        
        //then
        Category updatedCategory = em.find(Category.class, id);
        
        assertNotEquals("category != updatedCategory", category, findCategory);
        assertEquals("Name must be changed", "분류2", updatedCategory.getName());
    }
    
    @Test
    public void 삭제() {
        //given
        Category category = createCategory("분류");
        em.persist(category);
        Long id = category.getId();
        clear();
        
        //when
        Category findCategory = em.find(Category.class, id);
        categoryRepository.delete(findCategory);
        clear();
        
        //then
        assertFalse("Category must not be found", categoryRepository.findById(id).isPresent());
    }
    
    private Category createCategory(String name) {
        return Category.builder().name(name).build();
    }
    
}