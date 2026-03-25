package com.rentify.core.mapper;

import com.rentify.core.dto.payment.PaymentResponseDto;
import com.rentify.core.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "booking.status", target = "bookingStatus")
    PaymentResponseDto toDto(Payment payment);

    List<PaymentResponseDto> toDtos(List<Payment> payments);
}
