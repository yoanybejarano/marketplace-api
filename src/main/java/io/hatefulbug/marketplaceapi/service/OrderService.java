package io.hatefulbug.marketplaceapi.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hatefulbug.marketplaceapi.dto.CustomerDto;
import io.hatefulbug.marketplaceapi.dto.OrderDto;
import io.hatefulbug.marketplaceapi.entity.Customer;
import io.hatefulbug.marketplaceapi.entity.Location;
import io.hatefulbug.marketplaceapi.entity.Order;
import io.hatefulbug.marketplaceapi.entity.OrderItem;
import io.hatefulbug.marketplaceapi.entity.Product;
import io.hatefulbug.marketplaceapi.enums.OrderStatus;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.metric.OrderMetrics;
import io.hatefulbug.marketplaceapi.payload.OrderItemRequest;
import io.hatefulbug.marketplaceapi.payload.OrderRequest;
import io.hatefulbug.marketplaceapi.repository.LocationRepository;
import io.hatefulbug.marketplaceapi.repository.OrderRepository;
import io.hatefulbug.marketplaceapi.util.DtoMapperUtil;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final LocationRepository locationRepository;
    private final OrderMetrics orderMetrics;

    public OrderService(
            OrderRepository orderRepository,
            CustomerService customerService,
            ProductService productService,
            LocationRepository locationRepository,
            OrderMetrics orderMetrics
    ) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.locationRepository = locationRepository;
        this.orderMetrics = orderMetrics;
    }

    @Transactional
    public OrderDto placeOrder(OrderRequest orderRequest) {
        CustomerDto customerDto = customerService.getCustomerById(orderRequest.customerId());
        Customer customer = Customer.builder()
                .id(customerDto.getId())
                .firstName(customerDto.getFirstName())
                .lastName(customerDto.getLastName())
                .email(customerDto.getEmail())
                .phone(customerDto.getPhone())
                .createdAt(Instant.now())
                .build();

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(Instant.now());
        order.setStatus(OrderStatus.PROCESSING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemDto : orderRequest.items()) {
            Product product = productService.getProductById(itemDto.productId());

            // 1. Fetch or get a reference for the Location entity
            Location location = locationRepository.findById(itemDto.locationId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Location not found with ID: " + itemDto.locationId()));
            // Alternatively, use proxy to save a SELECT query if location is guaranteed valid:
            // Location location = locationRepository.getReferenceById(itemDto.locationId());

            productService.deductStock(product.getId(), itemDto.locationId(), itemDto.quantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setLocation(location); // <--- ADD THIS LINE
            orderItem.setQuantity(itemDto.quantity());
            orderItem.setUnitPrice(product.getPrice());

            orderItems.add(orderItem);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order orderResult = orderRepository.save(order);
        orderMetrics.recordOnlineOrderCreated();
        LOGGER.info("Order ID: {} placed successfully", orderResult.getId());
        return DtoMapperUtil.toOrderDto(orderResult);
    }

    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(status);
        LOGGER.info("Order ID: {} changed status to {}", order.getId(), status.name());
        orderRepository.save(order);
    }
}

