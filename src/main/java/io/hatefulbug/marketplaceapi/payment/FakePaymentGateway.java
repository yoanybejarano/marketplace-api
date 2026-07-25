package io.hatefulbug.marketplaceapi.payment;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class FakePaymentGateway implements PaymentGateway {

    private final Map<String, FakePayment> database = new ConcurrentHashMap<>();

    private final Random random = new Random();

    @Override
    public PaymentResponse authorize(PaymentRequest request) {

        FakePayment payment = new FakePayment();

        payment.setId(UUID.randomUUID().toString());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setCustomerId(request.getCustomerId());
        payment.setOrderId(request.getOrderId());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setCreatedAt(LocalDateTime.now());

        int probability = random.nextInt(100);

        if (probability < 80) {
            payment.setStatus(PaymentStatus.AUTHORIZED);
        } else if (probability < 90) {
            payment.setStatus(PaymentStatus.PENDING);
        } else {
            payment.setStatus(PaymentStatus.DECLINED);
        }

        database.put(payment.getId(), payment);
        return toResponse(payment);
    }

    @Override
    public PaymentResponse capture(String paymentId) {

        FakePayment payment = database.get(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found");
        }

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new RuntimeException("Payment cannot be captured");
        }


        payment.setStatus(PaymentStatus.CAPTURED);
        return toResponse(payment);
    }

    @Override
    public RefundResponse refund(String paymentId) {

        FakePayment payment = database.get(paymentId);

        if (payment == null) {
            throw new RuntimeException("Payment not found");
        }

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new RuntimeException("Only captured payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);

        RefundResponse response = new RefundResponse();

        response.setPaymentId(paymentId);
        response.setStatus(PaymentStatus.REFUNDED);
        response.setMessage("Refund successful");
        return response;
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {

        FakePayment payment = database.get(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found");
        }
        return toResponse(payment);
    }

    @Override
    public void cancel(String paymentId) {

        FakePayment payment = database.get(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
    }

    private PaymentResponse toResponse(FakePayment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setPaymentId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());

        switch (payment.getStatus()) {
            case AUTHORIZED -> response.setMessage("Payment Authorized");
            case DECLINED -> response.setMessage("Payment Declined");
            case PENDING -> response.setMessage("Pending Review");
            case CAPTURED -> response.setMessage("Payment Captured");
            case REFUNDED -> response.setMessage("Refund Successful");
            default -> response.setMessage(payment.getStatus().name());
        }

        return response;
    }
}
