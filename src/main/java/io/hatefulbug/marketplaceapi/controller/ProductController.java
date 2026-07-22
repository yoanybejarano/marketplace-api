package io.hatefulbug.marketplaceapi.controller;

import io.hatefulbug.marketplaceapi.dto.ProductDto;
import io.hatefulbug.marketplaceapi.request.PageResponse;
import io.hatefulbug.marketplaceapi.service.ProductService;
import io.hatefulbug.marketplaceapi.util.ConverterUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get all products")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<ProductDto>> getAllProducts(@RequestParam int page, @RequestParam int size) {
        PageResponse<ProductDto> products = productService.getAllProducts(page, size);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get products by category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PageResponse<ProductDto>> getProductsByCategory(@PathVariable Integer categoryId, @RequestParam int page, @RequestParam int size) {
        PageResponse<ProductDto> products = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer id) {
        ProductDto product = ConverterUtil.toProductDto(productService.getProductById(id));
        return ResponseEntity.ok(product);
    }

    // Note: productService.deductStock() is omitting a direct controller entry point
    // because inventory deduction is an internal business operation triggered by the checkout lifecycle.
}
