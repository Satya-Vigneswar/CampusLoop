package com.campusloop.campusloop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "marketplace_products")
public class Product {

    public static final List<String> CATEGORIES = List.of(
            "Books", "Electronics", "Stationery", "Hostel Essentials", "Sports", "Others");
    public static final List<String> CONDITIONS = List.of("New", "Like New", "Good", "Fair");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "condition", nullable = false, length = 20)
    private String condition;

    @Column(nullable = false, length = 100)
    private String sellerName;

    @Column(length = 25)
    private String sellerPhone;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Product() {
    }

    public Product(
            String name,
            String description,
            BigDecimal price,
            String category,
            String condition,
            String sellerName,
            String sellerPhone,
            Long sellerId,
            String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.sellerId = sellerId;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getCondition() {
        return condition;
    }

    public String getSellerName() {
        return sellerName;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
