package io.hatefulbug.marketplaceapi.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hatefulbug.marketplaceapi.dto.OrderDto;
import io.hatefulbug.marketplaceapi.request.OrderRequest;
import io.hatefulbug.marketplaceapi.request.StatusUpdateRequest;
import io.hatefulbug.marketplaceapi.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Place order")
    @PostMapping
    public ResponseEntity<OrderDto> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderDto createdOrder = orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @Operation(summary = "Update order status")
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateRequest statusRequest) {

        orderService.updateOrderStatus(id, statusRequest.status());
        return ResponseEntity.ok().build();
    }
}
