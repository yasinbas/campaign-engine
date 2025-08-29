package com.opensource.campaignengine.service;

import com.opensource.campaignengine.domain.Category;
import com.opensource.campaignengine.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(Long id) {
        log.debug("Finding category with id {}", id);
        return categoryRepository.findById(id);
    }

    public Category save(Category category) {
        log.info("Saving category {}", category.getName());
        return categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        log.info("Deleting category with id {}", id);
        categoryRepository.deleteById(id);
    }
}
