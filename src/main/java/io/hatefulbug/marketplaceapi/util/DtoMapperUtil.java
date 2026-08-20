package io.hatefulbug.marketplaceapi.util;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

public class DtoMapperUtil {

    private static final ObjectMapper DTO_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static OrderDto toOrderDto(Order order) {
        return DTO_MAPPER.convertValue(order, OrderDto.class);
    }

    public static List<OrderItemDto> convertOrderItemDto(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> DTO_MAPPER.convertValue(item, OrderItemDto.class))
                .collect(Collectors.toList());
    }

    public static OrderItemDto toOrderItemDto(OrderItem orderItem) {
        return DTO_MAPPER.convertValue(orderItem, OrderItemDto.class);
    }

    public static CustomerDto toCustomerDto(Customer customer) {
        return DTO_MAPPER.convertValue(customer, CustomerDto.class);
    }

    public static ProductDto toProductDto(Product product) {
        return DTO_MAPPER.convertValue(product, ProductDto.class);
    }

    public static CategoryDto toCategoryDto(Category category) {
        return DTO_MAPPER.convertValue(category, CategoryDto.class);
    }

    public static PaymentDto toPaymentDto(Payment payment) {
        return DTO_MAPPER.convertValue(payment, PaymentDto.class);
    }
}






















