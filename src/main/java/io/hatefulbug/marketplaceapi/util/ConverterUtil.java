package io.hatefulbug.marketplaceapi.util;

import java.util.List;

import io.hatefulbug.marketplaceapi.dto.CategoryDto;
import io.hatefulbug.marketplaceapi.dto.CustomerDto;
import io.hatefulbug.marketplaceapi.dto.OrderDto;
import io.hatefulbug.marketplaceapi.dto.OrderItemDto;
import io.hatefulbug.marketplaceapi.dto.PaymentDto;
import io.hatefulbug.marketplaceapi.dto.ProductDto;
import io.hatefulbug.marketplaceapi.entity.Category;
import io.hatefulbug.marketplaceapi.entity.Customer;
import io.hatefulbug.marketplaceapi.entity.Order;
import io.hatefulbug.marketplaceapi.entity.OrderItem;
import io.hatefulbug.marketplaceapi.entity.Payment;
import io.hatefulbug.marketplaceapi.entity.Product;

public class ConverterUtil {

    public static OrderDto toOrderDto(Order order) {
        return new OrderDto(order.getId(),
                toCustomerDto(order.getCustomer()),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getTotalAmount(),
                convertOrderItemDto(order.getOrderItems()));
    }

    public static List<OrderItemDto> convertOrderItemDto(List<OrderItem> orderItems) {
        return orderItems.stream().map(ConverterUtil::toOrderItemDto).toList();
    }

    public static OrderItemDto toOrderItemDto(OrderItem orderItem) {
        return new OrderItemDto(orderItem.getId(), toProductDto(orderItem.getProduct()),
                orderItem.getQuantity(), orderItem.getUnitPrice());
    }

    public static CustomerDto toCustomerDto(Customer customer) {
        return new CustomerDto(customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt());
    }

    public static ProductDto toProductDto(Product product) {
        return new ProductDto(
                product.getId(),
                toCategoryDto(product.getCategory()),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku(),
                product.getImageUrl(),
                product.getCreatedAt()
        );
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    public static PaymentDto toPaymentDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                toOrderDto(payment.getOrder()),
                payment.getPaymentMethod().name(),
                payment.getPaymentStatus().name(),
                payment.getTransactionId(),
                payment.getPaymentDate()
        );
    }

}




















