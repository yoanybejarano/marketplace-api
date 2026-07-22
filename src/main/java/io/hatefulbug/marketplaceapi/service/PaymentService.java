package io.hatefulbug.marketplaceapi.service;

import io.hatefulbug.marketplaceapi.dto.PaymentDto;
import io.hatefulbug.marketplaceapi.entity.Order;
import io.hatefulbug.marketplaceapi.entity.Payment;
import io.hatefulbug.marketplaceapi.exception.ResourceNotFoundException;
import io.hatefulbug.marketplaceapi.dto.OrderStatus;
import io.hatefulbug.marketplaceapi.payment.PaymentGateway;
import io.hatefulbug.marketplaceapi.payment.PaymentRequest;
import io.hatefulbug.marketplaceapi.payment.PaymentResponse;
import io.hatefulbug.marketplaceapi.payment.PaymentStatus;
import io.hatefulbug.marketplaceapi.repository.OrderRepository;
import io.hatefulbug.marketplaceapi.repository.PaymentRepository;
import io.hatefulbug.marketplaceapi.util.ConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            OrderService orderService,
            PaymentGateway paymentGateway) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.paymentGateway = paymentGateway;
    }

    public PaymentDto processPayment(PaymentRequest request) {
        logger.info("Processing payment request for OrderID: {} | Method: {}", request.getOrderId(), request.getPaymentMethod());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    logger.warn("Payment processing aborted. Order not found for OrderID: {}", request.getOrderId());
                    return new ResourceNotFoundException("Order not found");
                });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(Instant.now());

        PaymentRequest gatewayRequest = new PaymentRequest(
                order.getTotalAmount(),
                "USD",
                request.getPaymentMethod(),
                order.getCustomer().getId(),
                order.getId()
        );

        logger.debug("Sending authorization request to gateway for OrderID: {} | Amount: USD {}", order.getId(), order.getTotalAmount());

        PaymentResponse response = paymentGateway.authorize(gatewayRequest);
        payment.setTransactionId(response.getPaymentId());
        logger.info("Gateway auth response received. OrderID: {} | Status: {} | TransactionID: {}",
                order.getId(), response.getStatus(), response.getPaymentId());

        switch (response.getStatus()) {

            case AUTHORIZED -> {
                logger.debug("Attempting to capture payment for TransactionID: {}", response.getPaymentId());
                PaymentResponse captureResponse = paymentGateway.capture(response.getPaymentId());

                payment.setPaymentStatus(PaymentStatus.CAPTURED);
                orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);
                payment.setGatewayMessage(captureResponse.getMessage());
                logger.info("Payment successfully captured. TransactionID: {} | OrderID: {} | Amount: USD {}",
                        response.getPaymentId(), order.getId(), order.getTotalAmount());
            }

            case PENDING -> {
                payment.setPaymentStatus(PaymentStatus.PENDING);
                orderService.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);
                payment.setGatewayMessage(response.getMessage());
                logger.info("Payment is pending external completion. OrderID: {} | TransactionID: {}", order.getId(), response.getPaymentId());
            }

            case DECLINED -> {
                payment.setPaymentStatus(PaymentStatus.DECLINED);
                orderService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
                payment.setGatewayMessage(response.getMessage());
                logger.warn("Payment declined by gateway. OrderID: {} | TransactionID: {} | Reason: {}",
                        order.getId(), response.getPaymentId(), response.getMessage());
            }

            case FAILED -> {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                orderService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
                payment.setGatewayMessage(response.getMessage());
                logger.error("Payment processing failed at gateway. OrderID: {} | TransactionID: {} | Error: {}",
                        order.getId(), response.getPaymentId(), response.getMessage());
            }

            case CANCELLED -> {
                payment.setPaymentStatus(PaymentStatus.CANCELLED);
                orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
                payment.setGatewayMessage(response.getMessage());
                logger.info("Payment cancelled. OrderID: {} | TransactionID: {}", order.getId(), response.getPaymentId());
            }

            default -> {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                orderService.updateOrderStatus(order.getId(), OrderStatus.FAILED);
                payment.setGatewayMessage("Unknown payment status");
                logger.error("Unexpected payment status encountered from gateway. OrderID: {} | Status: {}", order.getId(), response.getStatus());
            }
        }

        Payment paymentResult = paymentRepository.save(payment);
        logger.debug("Payment entity persisted successfully. PaymentID: {}", paymentResult.getId());
        return ConverterUtil.toPaymentDto(paymentResult);
    }

    public PaymentDto getPayment(Integer paymentId) {
        logger.debug("Fetching payment details for PaymentID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    logger.warn("Fetch failed. Payment record not found for PaymentID: {}", paymentId);
                    return new ResourceNotFoundException("Payment not found");
                });
        return ConverterUtil.toPaymentDto(payment);
    }

    public PaymentDto refund(Integer paymentId) {
        logger.info("Initiating refund request for PaymentID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    logger.warn("Refund aborted. Payment record not found for PaymentID: {}", paymentId);
                    return new ResourceNotFoundException("Payment not found");
                });
        String transactionId = payment.getTransactionId();
        Integer orderId = payment.getOrder().getId();

        logger.debug("Calling payment gateway to execute refund for TransactionID: {}", transactionId);
        paymentGateway.refund(transactionId);

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        orderService.updateOrderStatus(orderId, OrderStatus.REFUNDED);

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Refund processed successfully. TransactionID: {} | OrderID: {} | PaymentID: {}",
                transactionId, orderId, savedPayment.getId());
        return ConverterUtil.toPaymentDto(savedPayment);
    }

    public PaymentDto cancelPayment(Integer paymentId) {
        logger.info("Initiating payment cancellation for PaymentID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    logger.warn("Cancellation aborted. Payment record not found for PaymentID: {}", paymentId);
                    return new ResourceNotFoundException("Payment not found");
                });

        String transactionId = payment.getTransactionId();
        Integer orderId = payment.getOrder().getId();

        logger.debug("Calling payment gateway to void/cancel TransactionID: {}", transactionId);
        paymentGateway.cancel(transactionId);

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
        Payment savedPayment = paymentRepository.save(payment);

        logger.info("Payment cancelled successfully. TransactionID: {} | OrderID: {} | PaymentID: {}",
                transactionId, orderId, savedPayment.getId());
        return ConverterUtil.toPaymentDto(savedPayment);
    }
}
