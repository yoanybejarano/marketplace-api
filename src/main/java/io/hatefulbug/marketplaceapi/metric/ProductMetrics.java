package io.hatefulbug.marketplaceapi.metric;

import java.util.function.Supplier;

import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class ProductMetrics {

    private final MeterRegistry meterRegistry;
    private final Timer getAllProductsTimer;
    private final Timer getProductsByCategoryTimer;
    private final Timer deductStockTimer;
    private final Counter stockDeductionSuccessCounter;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "MeterRegistry is a thread-safe Spring-managed component meant to be shared."
    )
    public ProductMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.getAllProductsTimer = meterRegistry.timer("product.catalog.get_all.time");
        this.getProductsByCategoryTimer = meterRegistry.timer("product.catalog.get_by_category.time");
        this.deductStockTimer = meterRegistry.timer("product.stock.deduct.time");
        this.stockDeductionSuccessCounter = meterRegistry.counter("product.stock.deductions.success");
    }

    public <T> T recordGetAllProducts(Supplier<T> supplier) {
        return getAllProductsTimer.record(supplier);
    }

    public <T> T recordGetProductsByCategory(Supplier<T> supplier) {
        return getProductsByCategoryTimer.record(supplier);
    }

    public void recordDeductStock(Runnable runnable) {
        deductStockTimer.record(runnable);
    }

    public void recordDeductionSuccess() {
        stockDeductionSuccessCounter.increment();
    }

    public void recordDeductionFailure(String reason) {
        meterRegistry.counter("product.stock.deductions.failed", "reason", reason).increment();
    }
}
