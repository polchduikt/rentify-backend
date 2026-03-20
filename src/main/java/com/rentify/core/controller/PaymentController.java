package com.rentify.core.controller;

import com.rentify.core.dto.payment.PaymentResponseDto;
import com.rentify.core.service.PaymentService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment endpoints")
@SecurityRequirement(name = "bearerAuth")
@Validated
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/bookings/{bookingId}/mock-pay")
    @Operation(
            summary = "Mock pay booking",
            description = "Creates successful mock payment for booking in development/testing flow."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment created",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<PaymentResponseDto> mockPayBooking(
            @Parameter(description = "Booking ID", example = "55")
            @PathVariable @Positive Long bookingId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.payBooking(bookingId));
    }

    @GetMapping({"/my", "/me"})
    @Operation(
            summary = "Get current user payments",
            description = "Returns all payments that belong to the authenticated user account."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Payments retrieved",
            content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
    )
    public ResponseEntity<List<PaymentResponseDto>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/bookings/{bookingId}")
    @Operation(
            summary = "Get payments by booking id",
            description = "Returns payment history for a specific booking visible to booking participants."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments by booking retrieved",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByBooking(
            @Parameter(description = "Booking ID", example = "55")
            @PathVariable @Positive Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBooking(bookingId));
    }
}
