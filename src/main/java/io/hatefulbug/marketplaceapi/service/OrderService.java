package io.hatefulbug.marketplaceapi.service;

import io.hatefulbug.marketplaceapi.dto.CustomerDto;
import io.hatefulbug.marketplaceapi.dto.OrderDto;
import io.hatefulbug.marketplaceapi.entity.Customer;
import io.hatefulbug.marketplaceapi.entity.Order;
import io.hatefulbug.marketplaceapi.entity.OrderItem;
import io.hatefulbug.marketplaceapi.entity.Product;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.dto.OrderStatus;
import io.hatefulbug.marketplaceapi.request.OrderItemRequest;
import io.hatefulbug.marketplaceapi.request.OrderRequest;
import io.hatefulbug.marketplaceapi.repository.OrderRepository;
import io.hatefulbug.marketplaceapi.util.ConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                        CustomerService customerService,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
    }

    @Transactional
    public OrderDto placeOrder(OrderRequest orderRequest) {
        CustomerDto customerDto = customerService.getCustomerById(orderRequest.customerId());
        Customer customer = Customer.builder()
                .id(customerDto.id())
                .firstName(customerDto.firstName())
                .lastName(customerDto.lastName())
                .email(customerDto.email())
                .phone(customerDto.phone())
                .createdAt(Instant.now()).build();

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(Instant.now());
        order.setStatus(OrderStatus.PROCESSING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemDto : orderRequest.items()) {
            Product product = productService.getProductById(itemDto.productId());

            productService.deductStock(product.getId(), itemDto.quantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.quantity());
            orderItem.setUnitPrice(product.getPrice());

            orderItems.add(orderItem);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order orderResult = orderRepository.save(order);
        logger.info("Order ID: {} placed successfully", orderResult.getId());
        return ConverterUtil.toOrderDto(orderResult);
    }

    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(status);
        logger.info("Order ID: {} changed status to {}", order.getId(), status.name());
        orderRepository.save(order);
    }
}
