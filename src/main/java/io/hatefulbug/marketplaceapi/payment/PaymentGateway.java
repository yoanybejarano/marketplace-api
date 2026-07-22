package io.hatefulbug.marketplaceapi.payment;

public interface PaymentGateway {

    PaymentResponse authorize(PaymentRequest request);
    PaymentResponse capture(String paymentId);
    RefundResponse refund(String paymentId);
    PaymentResponse getPayment(String paymentId);
    void cancel(String paymentId);

}
