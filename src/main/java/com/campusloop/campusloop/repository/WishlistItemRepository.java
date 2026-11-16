package com.campusloop.campusloop.repository;

import com.campusloop.campusloop.entity.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    Optional<WishlistItem> findByStudentIdAndProduct_Id(Long studentId, Long productId);

    boolean existsByStudentIdAndProduct_Id(Long studentId, Long productId);

    @Transactional
    void deleteByProduct_Id(Long productId);
}
