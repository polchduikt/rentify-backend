package com.rentify.core.mapper;

import com.rentify.core.dto.user.PublicUserProfileDto;
import com.rentify.core.dto.user.UserResponseDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapStructCentralConfig.class)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponseDto toDto(User user);
    PublicUserProfileDto toPublicProfileDto(User user);
    List<UserResponseDto> toDtos(List<User> users);
    List<PublicUserProfileDto> toPublicProfileDtos(List<User> users);

    @BeanMapping(
            ignoreByDefault = true,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    void updateUser(UpdateUserRequestDto request, @MappingTarget User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
