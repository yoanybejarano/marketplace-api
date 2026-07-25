package io.hatefulbug.marketplaceapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hatefulbug.marketplaceapi.dto.ProductDto;
import io.hatefulbug.marketplaceapi.entity.Product;
import io.hatefulbug.marketplaceapi.exception.InsufficientStockException;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.repository.ProductRepository;
import io.hatefulbug.marketplaceapi.request.PageResponse;
import io.hatefulbug.marketplaceapi.util.ConverterUtil;
import io.hatefulbug.marketplaceapi.util.PageUtil;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResponse<ProductDto> getAllProducts(int page, int size) {
        Page<Product> pageResult = productRepository.findAll(PageRequest.of(page, size));
        Page<ProductDto> dtoPage = pageResult.map(ConverterUtil::toProductDto);
        return PageUtil.getPage(dtoPage);
    }

    public PageResponse<ProductDto> getProductsByCategory(Integer categoryId, int page, int size) {
        Page<Product> pageResult = productRepository.findByCategoryId(categoryId, PageRequest.of(page, size));
        Page<ProductDto> dtoPage = pageResult.map(ConverterUtil::toProductDto);
        return PageUtil.getPage(dtoPage);
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public void deductStock(Integer productId, int quantity) {
        LOGGER.debug("Attempting to deduct stock. ProductID: {} | Quantity: {}", productId, quantity);

        Product product = getProductById(productId);
        if (product.getStockQuantity() < quantity) {
            LOGGER.warn("Stock deduction rejected. Insufficient inventory for ProductID: " +
                            "{} ({}) | Available: {} | Requested: {}",
                    productId, product.getName(), product.getStockQuantity(), quantity);
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        int oldStock = product.getStockQuantity();
        int newStock = oldStock - quantity;

        product.setStockQuantity(newStock);
        productRepository.save(product);
        LOGGER.info("Stock deducted successfully. ProductID: {} ({}) | Deducted: {} | Prev Stock: {} | New Stock: {}",
                productId, product.getName(), quantity, oldStock, newStock);
    }

}

