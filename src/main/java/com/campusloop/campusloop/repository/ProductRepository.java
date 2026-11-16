package com.campusloop.campusloop.repository;

import com.campusloop.campusloop.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

    List<Product> findByCategoryOrderByCreatedAtDesc(String category);

    List<Product> findByNameContainingIgnoreCaseAndCategoryOrderByCreatedAtDesc(String name, String category);

    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);
}
