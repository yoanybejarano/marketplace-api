
package io.hatefulbug.marketplaceapi.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hatefulbug.marketplaceapi.dto.PaymentDto;
import io.hatefulbug.marketplaceapi.entity.Customer;
import io.hatefulbug.marketplaceapi.entity.Order;
import io.hatefulbug.marketplaceapi.entity.Payment;
import io.hatefulbug.marketplaceapi.enums.OrderStatus;
import io.hatefulbug.marketplaceapi.enums.PaymentMethod;
import io.hatefulbug.marketplaceapi.enums.PaymentStatus;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.metric.PaymentMetrics;
import io.hatefulbug.marketplaceapi.payment.PaymentGateway;
import io.hatefulbug.marketplaceapi.payment.PaymentRequest;
import io.hatefulbug.marketplaceapi.payment.PaymentResponse;
import io.hatefulbug.marketplaceapi.repository.OrderRepository;
import io.hatefulbug.marketplaceapi.repository.PaymentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentMetrics paymentMetrics;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<PaymentRequest> gatewayRequestCaptor;

    private PaymentService paymentService;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                orderRepository,
                orderService,
                paymentGateway,
                paymentMetrics
        );

        Customer sampleCustomer = new Customer();
        sampleCustomer.setId(10);

        sampleOrder = new Order();
        sampleOrder.setId(100);
        sampleOrder.setTotalAmount(new BigDecimal("150.00"));
        sampleOrder.setCustomer(sampleCustomer);
        sampleOrder.setStatus(OrderStatus.PAID);

        // Passthrough execution for recordPaymentProcessing supplier wrapper
        lenient().doAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        }).when(paymentMetrics).recordPaymentProcessing(any());
    }

    @Nested
    @DisplayName("processPayment Tests")
    class ProcessPaymentTests {

        @Test
        @DisplayName("AUTHORIZED status: Should authorize, capture, set status to CAPTURED and order to PAID")
        void processPayment_StatusAuthorized_CapturesAndSetsPaid() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(100);
            request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            PaymentResponse authResponse = new PaymentResponse();
            authResponse.setStatus(PaymentStatus.AUTHORIZED);
            authResponse.setPaymentId("TXN-12345");

            PaymentResponse captureResponse = new PaymentResponse();
            captureResponse.setMessage("Capture successful");

            when(orderRepository.findById(100))
                    .thenReturn(Optional.of(sampleOrder));

            when(paymentGateway.authorize(any(PaymentRequest.class)))
                    .thenReturn(authResponse);

            when(paymentGateway.capture("TXN-12345"))
                    .thenReturn(captureResponse);

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentDto result = paymentService.processPayment(request);

            // Then
            assertThat(result).isNotNull();

            verify(paymentGateway).authorize(gatewayRequestCaptor.capture());

            PaymentRequest gatewayRequest = gatewayRequestCaptor.getValue();

            assertThat(gatewayRequest.getAmount())
                    .isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(gatewayRequest.getCurrency()).isEqualTo("USD");
            assertThat(gatewayRequest.getPaymentMethod())
                    .isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(gatewayRequest.getCustomerId()).isEqualTo(10);
            assertThat(gatewayRequest.getOrderId()).isEqualTo(100);

            verify(paymentGateway).capture("TXN-12345");
            verify(orderService).updateOrderStatus(100, OrderStatus.PAID);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();

            assertThat(savedPayment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.CAPTURED);
            assertThat(savedPayment.getGatewayMessage())
                    .isEqualTo("Capture successful");
            assertThat(savedPayment.getTransactionId())
                    .isEqualTo("TXN-12345");

            // Metric verifications
            verify(paymentMetrics).recordPaymentProcessing(any());
            verify(paymentMetrics).recordPaymentStatus("CAPTURED", "CREDIT_CARD");
        }

        @Test
        @DisplayName("PENDING status: Should set payment status PENDING and order status PROCESSING")
        void processPayment_StatusPending() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(100);
            request.setPaymentMethod(PaymentMethod.PAYPAL);

            PaymentResponse response = new PaymentResponse();
            response.setStatus(PaymentStatus.PENDING);
            response.setPaymentId("TXN-PENDING-1");
            response.setMessage("Awaiting customer action");

            when(orderRepository.findById(100))
                    .thenReturn(Optional.of(sampleOrder));

            when(paymentGateway.authorize(any(PaymentRequest.class)))
                    .thenReturn(response);

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentDto result = paymentService.processPayment(request);

            // Then
            assertThat(result).isNotNull();

            verify(paymentGateway, never()).capture(any());
            verify(orderService).updateOrderStatus(100, OrderStatus.PROCESSING);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();

            assertThat(savedPayment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.PENDING);
            assertThat(savedPayment.getGatewayMessage())
                    .isEqualTo("Awaiting customer action");

            verify(paymentMetrics).recordPaymentStatus("PENDING", "PAYPAL");
        }

        @Test
        @DisplayName("DECLINED status: Should set payment status DECLINED and order status FAILED")
        void processPayment_StatusDeclined() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(100);
            request.setPaymentMethod(PaymentMethod.PAYPAL);

            PaymentResponse response = new PaymentResponse();
            response.setStatus(PaymentStatus.DECLINED);
            response.setPaymentId("TXN-DEC-1");
            response.setMessage("Insufficient funds");

            when(orderRepository.findById(100))
                    .thenReturn(Optional.of(sampleOrder));

            when(paymentGateway.authorize(any(PaymentRequest.class)))
                    .thenReturn(response);

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            paymentService.processPayment(request);

            // Then
            verify(orderService).updateOrderStatus(100, OrderStatus.FAILED);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();

            assertThat(savedPayment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.DECLINED);
            assertThat(savedPayment.getGatewayMessage())
                    .isEqualTo("Insufficient funds");

            verify(paymentMetrics).recordPaymentStatus("DECLINED", "PAYPAL");
        }

        @Test
        @DisplayName("FAILED status: Should set payment status FAILED and order status FAILED")
        void processPayment_StatusFailed() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(100);
            request.setPaymentMethod(PaymentMethod.PAYPAL);

            PaymentResponse response = new PaymentResponse();
            response.setStatus(PaymentStatus.FAILED);
            response.setPaymentId("TXN-FAIL-1");
            response.setMessage("Gateway timeout");

            when(orderRepository.findById(100))
                    .thenReturn(Optional.of(sampleOrder));

            when(paymentGateway.authorize(any(PaymentRequest.class)))
                    .thenReturn(response);

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            paymentService.processPayment(request);

            // Then
            verify(orderService).updateOrderStatus(100, OrderStatus.FAILED);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();

            assertThat(savedPayment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.FAILED);
            assertThat(savedPayment.getGatewayMessage())
                    .isEqualTo("Gateway timeout");

            verify(paymentMetrics).recordPaymentStatus("FAILED", "PAYPAL");
        }

        @Test
        @DisplayName("CANCELLED status: Should set payment status CANCELLED and order status CANCELLED")
        void processPayment_StatusCancelled() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(100);
            request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            PaymentResponse response = new PaymentResponse();
            response.setStatus(PaymentStatus.CANCELLED);
            response.setPaymentId("TXN-CAN-1");
            response.setMessage("User cancelled");

            when(orderRepository.findById(100))
                    .thenReturn(Optional.of(sampleOrder));

            when(paymentGateway.authorize(any(PaymentRequest.class)))
                    .thenReturn(response);

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            paymentService.processPayment(request);

            // Then
            verify(orderService).updateOrderStatus(100, OrderStatus.CANCELLED);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();

            assertThat(savedPayment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.CANCELLED);

            verify(paymentMetrics).recordPaymentStatus("CANCELLED", "CREDIT_CARD");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order does not exist")
        void processPayment_OrderNotFound_ThrowsException() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(999);
            request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            when(orderRepository.findById(999))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> paymentService.processPayment(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Order not found");

            verifyNoInteractions(paymentGateway);
            verifyNoInteractions(orderService);
            verify(paymentRepository, never()).save(any());

            verify(paymentMetrics).recordPaymentStatus("ABORTED", "CREDIT_CARD");
            verify(paymentMetrics).recordPaymentProcessing(any());
        }
    }

    @Nested
    @DisplayName("getPayment Tests")
    class GetPaymentTests {

        @Test
        @DisplayName("Should return PaymentDto when payment exists")
        void getPayment_WhenExists_ReturnsPaymentDto() {
            // Given
            Integer paymentId = 50;

            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setOrder(sampleOrder);

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(payment));

            // When
            PaymentDto result = paymentService.getPayment(paymentId);

            // Then
            assertThat(result).isNotNull();

            verify(paymentRepository).findById(paymentId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when payment does not exist")
        void getPayment_WhenNotFound_ThrowsException() {
            // Given
            Integer paymentId = 999;

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Payment not found");

            verify(paymentRepository).findById(paymentId);
        }
    }

    @Nested
    @DisplayName("refund Tests")
    class RefundTests {

        @Test
        @DisplayName("Should execute refund via gateway, update payment status to REFUNDED, and update order status")
        void refund_Success() {
            // Given
            Integer paymentId = 50;

            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setTransactionId("TXN-REFUND-100");
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            payment.setOrder(sampleOrder);

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(payment));

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentDto result = paymentService.refund(paymentId);

            // Then
            assertThat(result).isNotNull();

            verify(paymentGateway).refund("TXN-REFUND-100");
            verify(orderService).updateOrderStatus(100, OrderStatus.REFUNDED);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();

            assertThat(savedPayment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.REFUNDED);

            verify(paymentMetrics).recordRefund();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when payment to refund is missing")
        void refund_PaymentNotFound_ThrowsException() {
            // Given
            Integer paymentId = 999;

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> paymentService.refund(paymentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Payment not found");

            verifyNoInteractions(paymentGateway);
            verifyNoInteractions(orderService);
            verify(paymentRepository).findById(paymentId);
        }
    }

    @Nested
    @DisplayName("cancelPayment Tests")
    class CancelPaymentTests {

        @Test
        @DisplayName("Should void transaction via gateway, update payment status to CANCELLED, and update order status")
        void cancelPayment_Success() {
            // Given
            Integer paymentId = 50;

            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setTransactionId("TXN-VOID-100");
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            payment.setOrder(sampleOrder);

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(payment));

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentDto result = paymentService.cancelPayment(paymentId);

            // Then
            assertThat(result).isNotNull();

            verify(paymentGateway).cancel("TXN-VOID-100");
            verify(orderService).updateOrderStatus(100, OrderStatus.CANCELLED);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();

            assertThat(savedPayment.getPaymentStatus())
                    .isEqualTo(PaymentStatus.CANCELLED);

            verify(paymentMetrics).recordCancellation();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when payment to cancel is missing")
        void cancelPayment_PaymentNotFound_ThrowsException() {
            // Given
            Integer paymentId = 999;

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> paymentService.cancelPayment(paymentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Payment not found");

            verifyNoInteractions(paymentGateway);
            verifyNoInteractions(orderService);
            verify(paymentRepository).findById(paymentId);
        }
    }
}
