package com.rentify.core.security;

import com.rentify.core.entity.User;

public final class UserRoleUtils {

    private UserRoleUtils() {
    }

    public static boolean isAdmin(User user) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> Roles.ADMIN.equals(role.getName()));
    }
}
