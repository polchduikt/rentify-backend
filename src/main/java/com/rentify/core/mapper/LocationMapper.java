package com.rentify.core.mapper;

import com.rentify.core.dto.location.LocationSuggestionDto;
import com.rentify.core.entity.City;
import com.rentify.core.entity.District;
import com.rentify.core.entity.MetroStation;
import com.rentify.core.entity.ResidentialComplex;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface LocationMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(target = "type", constant = "CITY")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "id", target = "cityId")
    @Mapping(source = "name", target = "cityName")
    @Mapping(source = "region", target = "region")
    @Mapping(source = "country", target = "country")
    LocationSuggestionDto toCitySuggestion(City city);

    List<LocationSuggestionDto> toCitySuggestions(List<City> cities);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "type", constant = "DISTRICT")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "city.id", target = "cityId")
    @Mapping(source = "city.name", target = "cityName")
    @Mapping(source = "city.region", target = "region")
    @Mapping(source = "city.country", target = "country")
    LocationSuggestionDto toDistrictSuggestion(District district);

    List<LocationSuggestionDto> toDistrictSuggestions(List<District> districts);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "type", constant = "METRO")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "city.id", target = "cityId")
    @Mapping(source = "city.name", target = "cityName")
    @Mapping(source = "city.region", target = "region")
    @Mapping(source = "city.country", target = "country")
    LocationSuggestionDto toMetroSuggestion(MetroStation station);

    List<LocationSuggestionDto> toMetroSuggestions(List<MetroStation> stations);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "type", constant = "RESIDENTIAL_COMPLEX")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "city.id", target = "cityId")
    @Mapping(source = "city.name", target = "cityName")
    @Mapping(source = "city.region", target = "region")
    @Mapping(source = "city.country", target = "country")
    LocationSuggestionDto toResidentialComplexSuggestion(ResidentialComplex complex);

    List<LocationSuggestionDto> toResidentialComplexSuggestions(List<ResidentialComplex> complexes);
}
