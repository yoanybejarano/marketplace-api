
package io.hatefulbug.marketplaceapi.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hatefulbug.marketplaceapi.dto.CustomerDto;
import io.hatefulbug.marketplaceapi.dto.OrderDto;
import io.hatefulbug.marketplaceapi.entity.Location;
import io.hatefulbug.marketplaceapi.entity.Order;
import io.hatefulbug.marketplaceapi.entity.OrderItem;
import io.hatefulbug.marketplaceapi.entity.Product;
import io.hatefulbug.marketplaceapi.enums.OrderStatus;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.metric.OrderMetrics;
import io.hatefulbug.marketplaceapi.repository.LocationRepository;
import io.hatefulbug.marketplaceapi.repository.OrderRepository;
import io.hatefulbug.marketplaceapi.request.OrderItemRequest;
import io.hatefulbug.marketplaceapi.request.OrderRequest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @Mock
    private LocationRepository locationRepository;

    private OrderService orderService;
    private SimpleMeterRegistry meterRegistry;
    private OrderMetrics orderMetrics;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private CustomerDto sampleCustomerDto;
    private Product sampleProduct1;
    private Product sampleProduct2;
    private Location sampleLocation1;
    private Location sampleLocation2;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        orderMetrics = new OrderMetrics(meterRegistry);

        orderService = new OrderService(
                orderRepository,
                customerService,
                productService,
                locationRepository,
                orderMetrics
        );

        sampleCustomerDto = new CustomerDto(
                1,
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "9876543210",
                Instant.now()
        );

        sampleProduct1 = new Product();
        sampleProduct1.setId(101);
        sampleProduct1.setName("Product A");
        sampleProduct1.setPrice(new BigDecimal("25.00"));

        sampleProduct2 = new Product();
        sampleProduct2.setId(102);
        sampleProduct2.setName("Product B");
        sampleProduct2.setPrice(new BigDecimal("15.50"));

        sampleLocation1 = new Location();
        sampleLocation1.setId(201);

        sampleLocation2 = new Location();
        sampleLocation2.setId(202);
    }

    @Nested
    @DisplayName("placeOrder Tests")
    class PlaceOrderTests {

        @Test
        @DisplayName("Should successfully place an order and correctly calculate total amount")
        void placeOrder_Success() {
            // Given
            OrderItemRequest item1 =
                    new OrderItemRequest(101, 201, 2);

            OrderItemRequest item2 =
                    new OrderItemRequest(102, 202, 1);

            OrderRequest orderRequest =
                    new OrderRequest(1, List.of(item1, item2));

            when(customerService.getCustomerById(1))
                    .thenReturn(sampleCustomerDto);

            when(productService.getProductById(101))
                    .thenReturn(sampleProduct1);

            when(productService.getProductById(102))
                    .thenReturn(sampleProduct2);

            when(locationRepository.findById(201))
                    .thenReturn(Optional.of(sampleLocation1));

            when(locationRepository.findById(202))
                    .thenReturn(Optional.of(sampleLocation2));

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> {
                        Order savedOrder = invocation.getArgument(0);
                        savedOrder.setId(500);
                        return savedOrder;
                    });

            // When
            OrderDto result = orderService.placeOrder(orderRequest);

            // Then
            verify(customerService).getCustomerById(1);

            verify(productService).getProductById(101);
            verify(productService).deductStock(101, 201, 2);

            verify(productService).getProductById(102);
            verify(productService).deductStock(102, 202, 1);

            verify(locationRepository).findById(201);
            verify(locationRepository).findById(202);

            verify(orderRepository).save(orderCaptor.capture());

            Order capturedOrder = orderCaptor.getValue();

            assertThat(capturedOrder.getStatus())
                    .isEqualTo(OrderStatus.PROCESSING);

            assertThat(capturedOrder.getCustomer().getId())
                    .isEqualTo(1);

            assertThat(capturedOrder.getCustomer().getEmail())
                    .isEqualTo("jane.doe@example.com");

            assertThat(capturedOrder.getOrderItems())
                    .hasSize(2);

            assertThat(capturedOrder.getTotalAmount())
                    .isEqualByComparingTo(new BigDecimal("65.50"));

            // Verify first order item
            OrderItem capturedItem1 = capturedOrder.getOrderItems().get(0);

            assertThat(capturedItem1.getProduct().getId())
                    .isEqualTo(101);

            assertThat(capturedItem1.getLocation().getId())
                    .isEqualTo(201);

            assertThat(capturedItem1.getQuantity())
                    .isEqualTo(2);

            assertThat(capturedItem1.getUnitPrice())
                    .isEqualByComparingTo(new BigDecimal("25.00"));

            // Verify second order item
            OrderItem capturedItem2 = capturedOrder.getOrderItems().get(1);

            assertThat(capturedItem2.getProduct().getId())
                    .isEqualTo(102);

            assertThat(capturedItem2.getLocation().getId())
                    .isEqualTo(202);

            assertThat(capturedItem2.getQuantity())
                    .isEqualTo(1);

            assertThat(capturedItem2.getUnitPrice())
                    .isEqualByComparingTo(new BigDecimal("15.50"));

            assertThat(result).isNotNull();

            // Verify order metric
            assertThat(meterRegistry.get("orders.created")
                    .tag("type", "online")
                    .counter()
                    .count())
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer is not found")
        void placeOrder_CustomerNotFound_ThrowsException() {
            // Given
            OrderRequest orderRequest =
                    new OrderRequest(
                            99,
                            List.of(new OrderItemRequest(101, 201, 1))
                    );

            when(customerService.getCustomerById(99))
                    .thenThrow(
                            new ResourceNotFoundException(
                                    "Customer not found with id: 99"
                            )
                    );

            // When / Then
            assertThatThrownBy(
                    () -> orderService.placeOrder(orderRequest)
            )
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Customer not found with id: 99");

            verify(customerService).getCustomerById(99);

            verifyNoInteractions(productService);
            verifyNoInteractions(locationRepository);
            verifyNoInteractions(orderRepository);

            assertThat(meterRegistry.get("orders.created")
                    .tag("type", "online")
                    .counter()
                    .count())
                    .isZero();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when location is not found")
        void placeOrder_LocationNotFound_ThrowsException() {
            // Given
            OrderRequest orderRequest =
                    new OrderRequest(
                            1,
                            List.of(new OrderItemRequest(101, 999, 1))
                    );

            when(customerService.getCustomerById(1))
                    .thenReturn(sampleCustomerDto);

            when(productService.getProductById(101))
                    .thenReturn(sampleProduct1);

            when(locationRepository.findById(999))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(
                    () -> orderService.placeOrder(orderRequest)
            )
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Location not found with ID: 999");

            verify(customerService).getCustomerById(1);
            verify(productService).getProductById(101);
            verify(locationRepository).findById(999);

            // Stock must not be deducted because location validation
            // happens before deductStock().
            verify(productService, never())
                    .deductStock(101, 999, 1);

            verify(orderRepository, never()).save(any());

            assertThat(meterRegistry.get("orders.created")
                    .tag("type", "online")
                    .counter()
                    .count())
                    .isZero();
        }

        @Test
        @DisplayName("Should stop and throw exception when stock deduction fails")
        void placeOrder_InsufficientStock_ThrowsException() {
            // Given
            OrderItemRequest item =
                    new OrderItemRequest(101, 201, 100);

            OrderRequest orderRequest =
                    new OrderRequest(1, List.of(item));

            when(customerService.getCustomerById(1))
                    .thenReturn(sampleCustomerDto);

            when(productService.getProductById(101))
                    .thenReturn(sampleProduct1);

            when(locationRepository.findById(201))
                    .thenReturn(Optional.of(sampleLocation1));

            doThrow(new IllegalArgumentException("Insufficient stock"))
                    .when(productService)
                    .deductStock(101, 201, 100);

            // When / Then
            assertThatThrownBy(
                    () -> orderService.placeOrder(orderRequest)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient stock");

            verify(customerService).getCustomerById(1);
            verify(productService).getProductById(101);
            verify(locationRepository).findById(201);
            verify(productService).deductStock(101, 201, 100);

            verify(orderRepository, never()).save(any());

            assertThat(meterRegistry.get("orders.created")
                    .tag("type", "online")
                    .counter()
                    .count())
                    .isZero();
        }
    }

    @Nested
    @DisplayName("updateOrderStatus Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should successfully update order status when order exists")
        void updateOrderStatus_Success() {
            // Given
            Integer orderId = 500;

            Order existingOrder = new Order();
            existingOrder.setId(orderId);
            existingOrder.setStatus(OrderStatus.PROCESSING);

            when(orderRepository.findById(orderId))
                    .thenReturn(Optional.of(existingOrder));

            // When
            orderService.updateOrderStatus(
                    orderId,
                    OrderStatus.PAID
            );

            // Then
            verify(orderRepository).findById(orderId);
            verify(orderRepository).save(orderCaptor.capture());

            Order updatedOrder = orderCaptor.getValue();

            assertThat(updatedOrder.getId())
                    .isEqualTo(orderId);

            assertThat(updatedOrder.getStatus())
                    .isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order to update does not exist")
        void updateOrderStatus_OrderNotFound_ThrowsException() {
            // Given
            Integer orderId = 999;

            when(orderRepository.findById(orderId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(
                    () -> orderService.updateOrderStatus(
                            orderId,
                            OrderStatus.PROCESSING
                    )
            )
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Order not found with id: " + orderId);

            verify(orderRepository).findById(orderId);
            verify(orderRepository, never()).save(any());
        }
    }
}
