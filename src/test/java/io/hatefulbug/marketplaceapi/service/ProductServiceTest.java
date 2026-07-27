
package io.hatefulbug.marketplaceapi.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import io.hatefulbug.marketplaceapi.dto.ProductDto;
import io.hatefulbug.marketplaceapi.entity.Category;
import io.hatefulbug.marketplaceapi.entity.Product;
import io.hatefulbug.marketplaceapi.exception.InsufficientStockException;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.repository.ProductRepository;
import io.hatefulbug.marketplaceapi.request.PageResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private Product sampleProduct;
    private Category category;

    @BeforeEach
    void setUp() {

        category = new Category();
        category.setId(1);
        category.setName("Electronics");
        category.setDescription("Electronic products");

        sampleProduct = new Product();
        sampleProduct.setId(101);
        sampleProduct.setName("Wireless Mouse");
        sampleProduct.setPrice(new BigDecimal("29.99"));
        sampleProduct.setStockQuantity(50);
        sampleProduct.setCategory(category);
    }

    @Nested
    @DisplayName("getAllProducts Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products successfully")
        void getAllProducts_Success() {
            // Given
            int page = 0;
            int size = 10;
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageRequest, 1);

            when(productRepository.findAll(pageRequest)).thenReturn(productPage);

            // When
            PageResponse<ProductDto> result = productService.getAllProducts(page, size);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).findAll(pageRequest);
            verifyNoMoreInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("getProductsByCategory Tests")
    class GetProductsByCategoryTests {

        @Test
        @DisplayName("Should return products for a specific category")
        void getProductsByCategory_Success() {
            // Given
            Integer categoryId = 5;
            int page = 0;
            int size = 10;
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageRequest, 1);

            when(productRepository.findByCategoryId(categoryId, pageRequest)).thenReturn(productPage);

            // When
            PageResponse<ProductDto> result = productService.getProductsByCategory(categoryId, page, size);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).findByCategoryId(categoryId, pageRequest);
            verifyNoMoreInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when product ID exists")
        void getProductById_WhenProductExists_ReturnsProduct() {
            // Given
            Integer productId = 101;
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

            // When
            Product result = productService.getProductById(productId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(productId);
            assertThat(result.getName()).isEqualTo("Wireless Mouse");
            assertThat(result.getStockQuantity()).isEqualTo(50);

            verify(productRepository).findById(productId);
            verifyNoMoreInteractions(productRepository);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product ID does not exist")
        void getProductById_WhenProductDoesNotExist_ThrowsException() {
            // Given
            Integer productId = 999;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productService.getProductById(productId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found with id: " + productId);

            verify(productRepository).findById(productId);
            verifyNoMoreInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("deductStock Tests")
    class DeductStockTests {

        @Test
        @DisplayName("Should deduct stock successfully when sufficient inventory exists")
        void deductStock_SufficientStock_Success() {
            // Given
            Integer productId = 101;
            int deductQuantity = 20; // Stock is 50, new stock should be 30
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

            // When
            productService.deductStock(productId, deductQuantity);

            // Then
            verify(productRepository).findById(productId);
            verify(productRepository).save(productCaptor.capture());

            Product updatedProduct = productCaptor.getValue();
            assertThat(updatedProduct.getId()).isEqualTo(productId);
            assertThat(updatedProduct.getStockQuantity()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should deduct stock to exact zero when requested quantity matches stock")
        void deductStock_ExactStock_Success() {
            // Given
            Integer productId = 101;
            int deductQuantity = 50; // Stock is 50, new stock should be 0
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

            // When
            productService.deductStock(productId, deductQuantity);

            // Then
            verify(productRepository).save(productCaptor.capture());
            assertThat(productCaptor.getValue().getStockQuantity()).isZero();
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available stock")
        void deductStock_InsufficientStock_ThrowsException() {
            // Given
            Integer productId = 101;
            int deductQuantity = 60; // Stock is 50
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

            // When / Then
            assertThatThrownBy(() -> productService.deductStock(productId, deductQuantity))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessage("Insufficient stock for product: " + sampleProduct.getName());

            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void deductStock_ProductNotFound_ThrowsException() {
            // Given
            Integer productId = 999;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> productService.deductStock(productId, 5))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found with id: " + productId);

            verify(productRepository, never()).save(any());
        }
    }
}
