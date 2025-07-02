package com.opensource.campaignengine.repository;

import com.opensource.campaignengine.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}