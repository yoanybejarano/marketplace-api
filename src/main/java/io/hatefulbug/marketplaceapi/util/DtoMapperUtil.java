package io.hatefulbug.marketplaceapi.util;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import io.hatefulbug.marketplaceapi.dto.CategoryDto;
import io.hatefulbug.marketplaceapi.dto.CustomerDto;
import io.hatefulbug.marketplaceapi.dto.InventoryDto;
import io.hatefulbug.marketplaceapi.dto.LocationDto;
import io.hatefulbug.marketplaceapi.dto.OrderDto;
import io.hatefulbug.marketplaceapi.dto.OrderItemDto;
import io.hatefulbug.marketplaceapi.dto.PaymentDto;
import io.hatefulbug.marketplaceapi.dto.ProductDto;
import io.hatefulbug.marketplaceapi.entity.Category;
import io.hatefulbug.marketplaceapi.entity.Customer;
import io.hatefulbug.marketplaceapi.entity.Inventory;
import io.hatefulbug.marketplaceapi.entity.Location;
import io.hatefulbug.marketplaceapi.entity.Order;
import io.hatefulbug.marketplaceapi.entity.OrderItem;
import io.hatefulbug.marketplaceapi.entity.Payment;
import io.hatefulbug.marketplaceapi.entity.Product;

public class DtoMapperUtil {

    public static OrderDto toOrderDto(Order order) {
        if (order == null) {
            return null;
        }

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCustomer(toCustomerDto(order.getCustomer()));

        if (order.getOrderItems() != null) {
            dto.setOrderItems(convertOrderItemDto(order.getOrderItems()));
        }

        return dto;
    }

    public static List<OrderItemDto> convertOrderItemDto(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return List.of();
        }
        return orderItems.stream()
                .map(DtoMapperUtil::toOrderItemDto)
                .collect(Collectors.toList());
    }

    public static OrderItemDto toOrderItemDto(OrderItem item) {
        if (item == null) {
            return null;
        }

        return new OrderItemDto(
                item.getId(),
                toProductDto(item.getProduct()),
                toLocationDto(item.getLocation()),
                item.getQuantity(),
                item.getUnitPrice()
        );
    }

    public static LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getCode(),
                location.getType(),
                location.getAddress(),
                location.getCity(),
                location.getState(),
                location.getZipCode(),
                location.getCountry(),
                location.isActive(),
                location.getCreatedAt() != null
                        ? location.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                        : null
        );
    }

    public static CustomerDto toCustomerDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }

    public static ProductDto toProductDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSku(product.getSku());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());

        if (product.getCategory() != null) {
            dto.setCategory(toCategoryDto(product.getCategory()));
        }

        if (product.getInventories() != null) {
            List<InventoryDto> inventoryDtos = product.getInventories().stream()
                    .map(DtoMapperUtil::toInventoryDto)
                    .collect(Collectors.toList());
            dto.setInventories(inventoryDtos);
        }

        dto.setStockQuantity(product.getStockQuantity());
        return dto;
    }

    public static CategoryDto toCategoryDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    public static InventoryDto toInventoryDto(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setQuantity(inventory.getQuantity());
        dto.setReservedQuantity(inventory.getReservedQuantity());
        dto.setAvailableQuantity(inventory.getAvailableQuantity());
        return dto;
    }

    public static PaymentDto toPaymentDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setOrder(toOrderDto(payment.getOrder()));
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setGateway(payment.getGateway());
        dto.setGatewayMessage(payment.getGatewayMessage());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAuthorizedAt(payment.getAuthorizedAt());
        dto.setCapturedAt(payment.getCapturedAt());
        return dto;
    }
}





















