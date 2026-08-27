package io.hatefulbug.marketplaceapi.metric;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OrderMetrics {

    private final Counter onlineOrderCounter;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.onlineOrderCounter = meterRegistry.counter("orders.created", "type", "online");
    }

    public void recordOnlineOrderCreated() {
        onlineOrderCounter.increment();
    }
}
