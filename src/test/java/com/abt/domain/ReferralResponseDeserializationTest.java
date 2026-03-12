package com.abt.domain;

import com.abt.util.CustomJacksonObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferralResponseDeserializationTest {

    private static final String PAYLOAD = "{\n"
            + "  \"gothomis_response\": {\n"
            + "    \"id\": 8740,\n"
            + "    \"referral_no\": \"57849\",\n"
            + "    \"received_feedback_payload\": {\n"
            + "      \"referralNo\": \"57849\",\n"
            + "      \"referralStatus\": \"ATTENDED\",\n"
            + "      \"servicesProvided\": [\n"
            + "        {\n"
            + "          \"hfrCode\": \"12201-1\",\n"
            + "          \"serviceCode\": \"feedback_remarks\",\n"
            + "          \"serviceName\": \"Client has attended safely and need more attention in facility.\",\n"
            + "          \"facilityName\": \"Sabasaba Health Center\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"referralFeedbackDate\": \"2026-03-12\"\n"
            + "    },\n"
            + "    \"received_date\": \"2026-03-12 10:44:42.915705+03\",\n"
            + "    \"processed_date\": null\n"
            + "  },\n"
            + "  \"event_metadata\": {\n"
            + "    \"locationid\": \"fbdd93f1-2045-4744-ae38-133f78a049c0\",\n"
            + "    \"providerid\": \"markchw\",\n"
            + "    \"team\": \"Kia - 102557-6\",\n"
            + "    \"baseentityid\": \"23a34f9d-3e69-40c0-97bf-9d428018c565\",\n"
            + "    \"teamid\": \"8b0ad916-115b-410c-9dd2-0b9fc00ca84f\",\n"
            + "    \"taskid\": \"61f33a9f-4d51-4845-a202-8fe325f5152d\"\n"
            + "  }\n"
            + "}";

    @Test
    void deserializesMissingOptionalResponseMetadataCollectionsAsEmptyLists() {
        ReferralResponse referralResponse = assertDoesNotThrow(
                () -> CustomJacksonObjectMapper.mapper.readValue(PAYLOAD, ReferralResponse.class)
        );

        assertNotNull(referralResponse);
        assertNotNull(referralResponse.getGothomisResponse());
        assertNull(referralResponse.getGothomisResponse().getProcessedDate());

        ReferralResponse.ResponseMetadata responseMetadata =
                referralResponse.getGothomisResponse().getResponseMetadata();

        assertNotNull(responseMetadata);
        assertEquals("ATTENDED", responseMetadata.getReferralStatus());
        assertNotNull(responseMetadata.getServicesProvided());
        assertEquals(1, responseMetadata.getServicesProvided().size());
        assertNotNull(responseMetadata.getPrescriptions());
        assertTrue(responseMetadata.getPrescriptions().isEmpty());
        assertNotNull(responseMetadata.getOutcomes());
        assertTrue(responseMetadata.getOutcomes().isEmpty());
    }
}
