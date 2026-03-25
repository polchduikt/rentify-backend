package com.rentify.core.mapper;

import com.rentify.core.dto.auth.AuthenticationResponseDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructCentralConfig.class)
public interface AuthenticationMapper {

    default AuthenticationResponseDto toAuthenticationResponse(String token) {
        return new AuthenticationResponseDto(token);
    }

    default AuthenticationResponseDto toEmptyAuthenticationResponse() {
        return new AuthenticationResponseDto(null);
    }
}
