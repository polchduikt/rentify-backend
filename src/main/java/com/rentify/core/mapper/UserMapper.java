package com.rentify.core.mapper;

import com.rentify.core.dto.user.PublicUserProfileDto;
import com.rentify.core.dto.user.UserResponseDto;
import com.rentify.core.entity.User;
import com.rentify.core.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponseDto toDto(User user);
    PublicUserProfileDto toPublicProfileDto(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
