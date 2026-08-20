package io.hatefulbug.marketplaceapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hatefulbug.marketplaceapi.dto.ProductDto;
import io.hatefulbug.marketplaceapi.entity.Inventory;
import io.hatefulbug.marketplaceapi.entity.Product;
import io.hatefulbug.marketplaceapi.exception.InsufficientStockException;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.repository.InventoryRepository;
import io.hatefulbug.marketplaceapi.repository.ProductRepository;
import io.hatefulbug.marketplaceapi.request.PageResponse;
import io.hatefulbug.marketplaceapi.util.DtoMapperUtil;
import io.hatefulbug.marketplaceapi.util.PageUtil;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository,
                          InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public PageResponse<ProductDto> getAllProducts(int page, int size) {
        Page<Product> pageResult = productRepository.findAll(PageRequest.of(page, size));
        Page<ProductDto> dtoPage = pageResult.map(DtoMapperUtil::toProductDto);
        return PageUtil.getPage(dtoPage);
    }

    public PageResponse<ProductDto> getProductsByCategory(Integer categoryId, int page, int size) {
        Page<Product> pageResult = productRepository.findByCategoryId(categoryId, PageRequest.of(page, size));
        Page<ProductDto> dtoPage = pageResult.map(DtoMapperUtil::toProductDto);
        return PageUtil.getPage(dtoPage);
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public void deductStock(
            Integer productId,
            Integer locationId,
            int quantity
    ) {
        LOGGER.debug(
                "Attempting to deduct stock. ProductID: {} | LocationID: {} | Quantity: {}",
                productId,
                locationId,
                quantity
        );

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Product product = getProductById(productId);

        Inventory inventory = inventoryRepository
                .findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product "
                                        + productId
                                        + " at location "
                                        + locationId
                        )
                );

        int availableStock = inventory.getAvailableQuantity();

        if (availableStock < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: "
                            + product.getName()
                            + " at the selected location"
            );
        }

        inventory.setQuantity(
                inventory.getQuantity() - quantity
        );

        LOGGER.info(
                "Stock deducted successfully. ProductID: {} | LocationID: {} | Quantity: {}",
                productId,
                locationId,
                quantity
        );
    }

}

