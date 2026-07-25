package io.hatefulbug.marketplaceapi.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String msg) {
        super(msg);
    }
}
