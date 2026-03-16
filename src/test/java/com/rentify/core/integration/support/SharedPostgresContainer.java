package com.rentify.core.integration.support;

import org.testcontainers.containers.PostgreSQLContainer;

public final class SharedPostgresContainer {

    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("rentify_test")
            .withUsername("rentify")
            .withPassword("rentify");

    static {
        INSTANCE.start();
    }

    private SharedPostgresContainer() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }
}
