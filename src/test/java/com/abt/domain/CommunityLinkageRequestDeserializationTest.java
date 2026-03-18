package com.abt.domain;

import com.abt.util.CustomJacksonObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommunityLinkageRequestDeserializationTest {

    private static final String PAYLOAD = "{\n"
            + "  \"identifiers\": {\n"
            + "    \"typeOfIdentifier\": \"UCS|MRN\",\n"
            + "    \"value\": \"1223141\"\n"
            + "  },\n"
            + "  \"firstName\": \"Tintu\",\n"
            + "  \"middleName\": \"Nyati\",\n"
            + "  \"lastName\": \"Maeda\",\n"
            + "  \"birthDate\": \"1960-03-06\",\n"
            + "  \"sex\": \"MALE\",\n"
            + "  \"mobileNumber\": \"0763810222\",\n"
            + "  \"maritalStatus\": \"married\",\n"
            + "  \"chwUsername\": \"tintu\",\n"
            + "  \"reason\": \"string\"\n"
            + "}";

    @Test
    void deserializesCommunityLinkagePayload() throws Exception {
        CommunityLinkageRequest request = CustomJacksonObjectMapper.mapper.readValue(PAYLOAD, CommunityLinkageRequest.class);

        assertNotNull(request.getIdentifiers());
        assertEquals("UCS|MRN", request.getIdentifiers().getTypeOfIdentifier());
        assertEquals("1223141", request.getIdentifiers().getValue());
        assertEquals("Tintu", request.getFirstName());
        assertEquals("Nyati", request.getMiddleName());
        assertEquals("Maeda", request.getLastName());
        assertEquals("1960-03-06", request.getBirthDate());
        assertEquals("MALE", request.getSex());
        assertEquals("0763810222", request.getMobileNumber());
        assertEquals("married", request.getMaritalStatus());
        assertEquals("tintu", request.getChwUsername());
        assertEquals("string", request.getReason());
    }
}
