package io.hatefulbug.marketplaceapi.metric;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class CustomerMetrics {

    private final Counter customerNotFoundCounter;
    private final Timer getCustomerTimer;

    public CustomerMetrics(MeterRegistry meterRegistry) {
        this.customerNotFoundCounter = meterRegistry.counter("customer.lookup.not_found");
        this.getCustomerTimer = meterRegistry.timer("customer.lookup.time");
    }

    public <T> T recordGetCustomer(Supplier<T> supplier) {
        return getCustomerTimer.record(supplier);
    }

    public void recordCustomerNotFound() {
        customerNotFoundCounter.increment();
    }
}
