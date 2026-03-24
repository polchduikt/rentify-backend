package com.rentify.core.controller;

import com.rentify.core.dto.booking.BookingDto;
import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.dto.payment.PaymentResponseDto;
import com.rentify.core.service.BookingService;
import com.rentify.core.service.PaymentService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking lifecycle endpoints for guests and hosts")
@SecurityRequirement(name = "bearerAuth")
@Validated
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Create booking",
            description = "Creates a booking request for the selected property and date range. Property must be available."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Booking created",
            content = @Content(schema = @Schema(implementation = BookingDto.class))
    )
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping
    @Operation(
            summary = "Get bookings",
            description = "Returns paginated bookings for the current user by role scope: guest or host."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Bookings retrieved",
            content = @Content(schema = @Schema(implementation = BookingDto.class))
    )
    public ResponseEntity<Page<BookingDto>> getBookings(
            @Parameter(description = "Scope of bookings: guest or host", example = "guest")
            @RequestParam(defaultValue = "guest") String role,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        if ("guest".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(bookingService.getMyBookings(pageable));
        }
        if ("host".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(bookingService.getIncomingBookings(pageable));
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported bookings role. Use guest or host.");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get booking by id",
            description = "Returns a booking if it belongs to the current guest or host."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking found",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDto> getById(
            @Parameter(description = "Booking ID", example = "55")
            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PostMapping("/{id}/cancellation")
    @Operation(
            summary = "Cancel booking",
            description = "Cancels booking in allowed state for current guest or host."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking canceled",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDto> cancelBooking(
            @Parameter(description = "Booking ID", example = "55")
            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PostMapping("/{id}/confirmation")
    @Operation(
            summary = "Confirm booking",
            description = "Confirms booking request in allowed state for current host."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking confirmed",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDto> confirmBooking(
            @Parameter(description = "Booking ID", example = "55")
            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @PostMapping("/{id}/rejection")
    @Operation(
            summary = "Reject booking",
            description = "Rejects booking request in allowed state for current host."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking rejected",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDto> rejectBooking(
            @Parameter(description = "Booking ID", example = "55")
            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(bookingService.rejectBooking(id));
    }

    @GetMapping("/{id}/payments")
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
            @PathVariable("id") @Positive Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBooking(bookingId));
    }
}
