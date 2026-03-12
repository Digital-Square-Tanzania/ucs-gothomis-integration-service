package com.abt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionPerformedSerializationTest {

    @Test
    void serializesActionPerformedAsJsonObject() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(
                new UcsGothomisIntegrationRegistry.ActionPerformed("sending successful")
        );

        assertEquals("{\"description\":\"sending successful\"}", json);
    }
}
