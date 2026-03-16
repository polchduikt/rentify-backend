package com.rentify.core.mapper;

import com.rentify.core.dto.favorite.FavoriteResponseDto;
import com.rentify.core.entity.Favorite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PropertyMapper.class)
public interface FavoriteMapper {

    @Mapping(source = "property.id", target = "propertyId")
    FavoriteResponseDto toDto(Favorite favorite);
}
