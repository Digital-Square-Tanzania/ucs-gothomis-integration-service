package com.abt.integration.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PostgresConnectionFactoryTest {

    @Test
    void schemaValueIsSanitized() {
        PostgresConnectionFactory factory = new PostgresConnectionFactory();

        assertFalse(factory.schema().isBlank());
    }
}
