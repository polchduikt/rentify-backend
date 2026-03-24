package com.rentify.core.mapper;

import com.rentify.core.dto.review.ReviewDto;
import com.rentify.core.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface ReviewMapper {
    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.firstName", target = "authorFirstName")
    ReviewDto toDto(Review review);

    List<ReviewDto> toDtos(List<Review> reviews);
}
