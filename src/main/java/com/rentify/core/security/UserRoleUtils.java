package com.rentify.core.security;

import com.rentify.core.entity.User;

public final class UserRoleUtils {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private UserRoleUtils() {
    }

    public static boolean isAdmin(User user) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> ADMIN_ROLE.equals(role.getName()));
    }
}
