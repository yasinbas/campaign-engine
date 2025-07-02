package com.opensource.campaignengine.repository;

import com.opensource.campaignengine.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}