package io.hatefulbug.marketplaceapi.payment;

public enum PaymentStatus {

    /**
     * Payment was created but has not yet been processed.
     */
    PENDING,

    /**
     * Funds have been authorized by the payment provider.
     */
    AUTHORIZED,

    /**
     * Funds have been captured successfully.
     */
    CAPTURED,

    /**
     * Payment was rejected by the payment provider.
     */
    DECLINED,

    /**
     * Payment failed due to a technical or business error.
     */
    FAILED,

    /**
     * Payment was cancelled before completion.
     */
    CANCELLED,

    /**
     * Payment has been refunded.
     */
    REFUNDED
}
