
package com.campusloop.campusloop;

import com.campusloop.campusloop.controller.ProductForm;
import com.campusloop.campusloop.entity.Product;
import com.campusloop.campusloop.entity.WishlistItem;
import com.campusloop.campusloop.repository.ProductRepository;
import com.campusloop.campusloop.repository.WishlistItemRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final ProductRepository productRepository;
    private final WishlistItemRepository wishlistItemRepository;

    public HomeController(ProductRepository productRepository, WishlistItemRepository wishlistItemRepository) {
        this.productRepository = productRepository;
        this.wishlistItemRepository = wishlistItemRepository;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            HttpSession session,
            Model model) {
        String search = q == null || q.isBlank() ? null : q.trim();
        String selectedCategory = category != null && Product.CATEGORIES.contains(category) ? category : null;

        List<Product> products;
        if (search == null && selectedCategory == null) {
            products = productRepository.findAllByOrderByCreatedAtDesc();
        } else if (search == null) {
            products = productRepository.findByCategoryOrderByCreatedAtDesc(selectedCategory);
        } else if (selectedCategory == null) {
            products = productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(search);
        } else {
            products = productRepository
                    .findByNameContainingIgnoreCaseAndCategoryOrderByCreatedAtDesc(search, selectedCategory);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", Product.CATEGORIES);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("searchQuery", search == null ? "" : search);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("savedProductIds", savedProductIds((Long) session.getAttribute("userId")));
        return "index";
    }

    @GetMapping("/products/new")
    public String newProduct(HttpSession session, Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", Product.CATEGORIES);
        model.addAttribute("conditions", Product.CONDITIONS);
        model.addAttribute("sellerName", session.getAttribute("userName"));
        return "product-form";
    }

    @PostMapping("/products")
    public String publishProduct(
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Product.CATEGORIES);
            model.addAttribute("conditions", Product.CONDITIONS);
            model.addAttribute("sellerName", session.getAttribute("userName"));
            return "product-form";
        }

        Product product = new Product(
                form.getName().trim(),
                form.getDescription().trim(),
                form.getPrice(),
                form.getCategory(),
                form.getCondition(),
                (String) session.getAttribute("userName"),
                form.getSellerPhone() == null || form.getSellerPhone().isBlank()
                        ? null
                        : form.getSellerPhone().trim(),
                (Long) session.getAttribute("userId"),
                form.getImageUrl().trim());
        Product savedProduct = productRepository.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Your item has been published.");
        return "redirect:/products/" + savedProduct.getId();
    }

    @GetMapping("/products/{id}")
    public String productDetails(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "That listing could not be found.");
            return "redirect:/";
        }
        model.addAttribute("product", product.get());
        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("currentUserId", studentId);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute(
                "inWishlist",
                wishlistItemRepository.existsByStudentIdAndProduct_Id(studentId, id));
        String sellerPhone = product.get().getSellerPhone();
        model.addAttribute(
                "whatsappNumber",
                sellerPhone == null ? "" : sellerPhone.replaceAll("\\D", ""));
        return "product-details";
    }

    @GetMapping("/my-listings")
    public String myListings(HttpSession session, Model model) {
        Long sellerId = (Long) session.getAttribute("userId");
        List<Product> products = productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        model.addAttribute("products", products);
        model.addAttribute("savedProductIds", savedProductIds(sellerId));
        model.addAttribute("userName", session.getAttribute("userName"));
        return "my-listings";
    }

    @GetMapping("/wishlist")
    public String wishlist(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("wishlistItems", wishlistItemRepository.findByStudentIdOrderByCreatedAtDesc(studentId));
        model.addAttribute("userName", session.getAttribute("userName"));
        return "wishlist";
    }

    @PostMapping("/wishlist/{productId}/toggle")
    public String toggleWishlist(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "false") boolean fromDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "That listing could not be found.");
            return "redirect:/";
        }

        Optional<WishlistItem> savedItem =
                wishlistItemRepository.findByStudentIdAndProduct_Id(studentId, productId);
        if (savedItem.isPresent()) {
            wishlistItemRepository.delete(savedItem.get());
            redirectAttributes.addFlashAttribute("successMessage", "Removed from your wishlist.");
        } else {
            wishlistItemRepository.save(new WishlistItem(studentId, product.get()));
            redirectAttributes.addFlashAttribute("successMessage", "Saved to your wishlist.");
        }

        return fromDetails ? "redirect:/products/" + productId : "redirect:/";
    }

    @PostMapping("/wishlist/{productId}/remove")
    public String removeFromWishlist(
            @PathVariable Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long studentId = (Long) session.getAttribute("userId");
        Optional<WishlistItem> savedItem =
                wishlistItemRepository.findByStudentIdAndProduct_Id(studentId, productId);
        if (savedItem.isPresent()) {
            wishlistItemRepository.delete(savedItem.get());
            redirectAttributes.addFlashAttribute("successMessage", "Removed from your wishlist.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "That item is not in your wishlist.");
        }
        return "redirect:/wishlist";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteListing(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long sellerId = (Long) session.getAttribute("userId");
        Optional<Product> product = productRepository.findByIdAndSellerId(id, sellerId);
        if (product.isPresent()) {
            wishlistItemRepository.deleteByProduct_Id(id);
            productRepository.delete(product.get());
            redirectAttributes.addFlashAttribute("successMessage", "Your listing has been removed.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only remove your own listings.");
        }
        return "redirect:/my-listings";
    }

    private Set<Long> savedProductIds(Long studentId) {
        return wishlistItemRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());
    }
}