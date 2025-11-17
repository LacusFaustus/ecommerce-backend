package com.ecommerce.onlinestore.controller;

import com.ecommerce.onlinestore.model.dto.ProductDTO;
import com.ecommerce.onlinestore.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a paginated list of all products")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a product by its SKU")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        ProductDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve products by category")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProducts(name, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/keyword")
    @Operation(summary = "Search products by keyword", description = "Search products by keyword in name or description")
    public ResponseEntity<Page<ProductDTO>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchByKeyword(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/price")
    @Operation(summary = "Filter products by price range", description = "Filter products by price range")
    public ResponseEntity<Page<ProductDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/category-price")
    @Operation(summary = "Filter products by category and price", description = "Filter products by category and price range")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategoryAndPriceRange(
            @RequestParam String category,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductsByCategoryAndPriceRange(
                category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available products", description = "Get products that are in stock")
    public ResponseEntity<Page<ProductDTO>> getAvailableProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getAvailableProducts(pageable);
        return ResponseEntity.ok(products);
    }
}
