package com.rentify.core.controller;

import com.rentify.core.dto.booking.BookingDto;
import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.service.BookingService;
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

    @GetMapping({"/my", "/me"})
    @Operation(
            summary = "Get my bookings",
            description = "Returns paginated bookings for the current user. Pagination params: page, size, sort."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Bookings retrieved",
            content = @Content(schema = @Schema(implementation = BookingDto.class))
    )
    public ResponseEntity<Page<BookingDto>> getMyBookings(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getMyBookings(pageable));
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

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel booking",
            description = "Cancels a booking according to current booking state and cancellation rules."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Booking canceled",
                    content = @Content(schema = @Schema(implementation = BookingDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDto> cancel(
            @Parameter(description = "Booking ID", example = "55")
            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @GetMapping("/incoming")
    @Operation(
            summary = "Get incoming bookings",
            description = "Returns paginated incoming bookings for host-owned properties."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Incoming bookings retrieved",
            content = @Content(schema = @Schema(implementation = BookingDto.class))
    )
    public ResponseEntity<Page<BookingDto>> getIncomingBookings(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.getIncomingBookings(pageable));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(
            summary = "Confirm booking",
            description = "Confirms a pending booking request. Available only for the property host."
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

    @PatchMapping("/{id}/reject")
    @Operation(
            summary = "Reject booking",
            description = "Rejects a pending booking request. Available only for the property host."
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
}
