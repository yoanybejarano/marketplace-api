package io.hatefulbug.marketplaceapi.metric;

import java.util.function.Supplier;

import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class PaymentMetrics {

    private final Timer processPaymentTimer;
    private final Counter refundCounter;
    private final Counter cancelCounter;
    private final MeterRegistry meterRegistry;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "MeterRegistry is a thread-safe Spring-managed component meant to be shared."
    )
    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.processPaymentTimer =
                meterRegistry.timer("payment.process.time");

        this.refundCounter =
                meterRegistry.counter("payment.refunds.count");

        this.cancelCounter =
                meterRegistry.counter("payment.cancellations.count");
    }

    public <T> T recordPaymentProcessing(Supplier<T> supplier) {
        return processPaymentTimer.record(supplier);
    }

    public void recordPaymentStatus(String status, String method) {
        meterRegistry.counter(
                "payment.outcomes",
                "status", status,
                "method", method
        ).increment();
    }

    public void recordRefund() {
        refundCounter.increment();
    }

    public void recordCancellation() {
        cancelCounter.increment();
    }
}
