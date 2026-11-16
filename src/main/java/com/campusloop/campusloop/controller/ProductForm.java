package com.campusloop.campusloop.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductForm {

    @NotBlank(message = "Enter a product name.")
    @Size(max = 100, message = "Product name must be 100 characters or fewer.")
    private String name;

    @NotBlank(message = "Add a short description.")
    @Size(max = 1000, message = "Description must be 1000 characters or fewer.")
    private String description;

    @NotNull(message = "Enter a price.")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero.")
    @Digits(integer = 8, fraction = 2, message = "Enter a price with up to 2 decimal places.")
    private BigDecimal price;

    @NotBlank(message = "Choose a category.")
    @Pattern(
            regexp = "Books|Electronics|Stationery|Hostel Essentials|Sports|Others",
            message = "Choose one of the listed categories.")
    private String category;

    @NotBlank(message = "Choose the product condition.")
    @Pattern(regexp = "New|Like New|Good|Fair", message = "Choose a listed condition.")
    private String condition;

    @NotBlank(message = "Add an image URL.")
    @Size(max = 2048, message = "Image URL must be 2048 characters or fewer.")
    @Pattern(regexp = "(?i)https?://\\S+", message = "Enter a valid HTTP or HTTPS image URL.")
    private String imageUrl;

    @Size(max = 25, message = "Phone number must be 25 characters or fewer.")
    @Pattern(
            regexp = "^$|\\+?[0-9][0-9\\s()-]{6,23}",
            message = "Enter a valid phone number, including its country code.")
    private String sellerPhone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }
}
