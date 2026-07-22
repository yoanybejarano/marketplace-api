package io.hatefulbug.marketplaceapi.controller;

import io.hatefulbug.marketplaceapi.dto.PaymentDto;
import io.hatefulbug.marketplaceapi.entity.Payment;
import io.hatefulbug.marketplaceapi.payment.PaymentRequest;
import io.hatefulbug.marketplaceapi.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Process payment")
    @PostMapping("/process")
    public ResponseEntity<PaymentDto> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentDto paymentResult = paymentService.processPayment(paymentRequest);
        return ResponseEntity.ok(paymentResult);
    }
}
