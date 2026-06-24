package com.abt.util;

import com.abt.domain.Event;
import com.abt.domain.Obs;
import com.abt.domain.ReferralResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OpenSrpServiceReferralResponseTest {

    @Test
    void buildsDiabetesHypertensionScreeningConfirmationEventFromNcdFinalDiagnosisOutcomes() throws Exception {
        ReferralResponse referralResponse = CustomJacksonObjectMapper.mapper.readValue(NCD_PAYLOAD, ReferralResponse.class);

        Event event = OpenSrpService.getDiabetesHypertensionScreeningConfirmationEvent(referralResponse);

        assertNotNull(event);
        assertEquals("Diabetes and Hypertension Screening Confirmation", event.getEventType());
        assertEquals("ec_diabetes_hypertension_confirmation", event.getEntityType());
        assertEquals("7aa6c349-6dcd-45e8-bf05-f53cec4ef6aa", event.getBaseEntityId());
        assertEquals("fbdd93f1-2045-4744-ae38-133f78a049c0", event.getLocationId());
        assertEquals("markchw", event.getProviderId());
        assertEquals("Kia - 102557-6", event.getTeam());
        assertEquals("8b0ad916-115b-410c-9dd2-0b9fc00ca84f", event.getTeamId());
        assertEquals("true", event.getDetails().get("detailsUpdated"));
        assertEquals(2, event.getObs().size());
        assertScreeningResult(event, "diabetes_result", "Positive");
        assertScreeningResult(event, "hypertension_result", "Positive");
    }

    @Test
    void mapsNcdFinalDiagnosisToNegativeWhenNoOutcomeIsTrue() throws Exception {
        String negativePayload = NCD_PAYLOAD
                .replace("\"diabeticClient\" : true", "\"diabeticClient\" : false")
                .replace("\"hypertensiveClient\" : true", "\"hypertensiveClient\" : false");
        ReferralResponse referralResponse = CustomJacksonObjectMapper.mapper.readValue(negativePayload, ReferralResponse.class);

        Event event = OpenSrpService.getDiabetesHypertensionScreeningConfirmationEvent(referralResponse);

        assertNotNull(event);
        assertScreeningResult(event, "diabetes_result", "Negative");
        assertScreeningResult(event, "hypertension_result", "Negative");
    }

    @Test
    void returnsNullWhenNcdFinalDiagnosisIsAbsent() throws Exception {
        ReferralResponse referralResponse = CustomJacksonObjectMapper.mapper.readValue(NO_NCD_PAYLOAD, ReferralResponse.class);

        assertNull(OpenSrpService.getDiabetesHypertensionScreeningConfirmationEvent(referralResponse));
    }

    private static void assertScreeningResult(Event event, String fieldCode, String result) {
        Obs obs = event.getObs().stream()
                .filter(candidate -> fieldCode.equals(candidate.getFieldCode()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing obs " + fieldCode));

        assertEquals("formsubmissionField", obs.getFieldType());
        assertEquals("text", obs.getFieldDataType());
        assertEquals(fieldCode, obs.getFormSubmissionField());
        assertEquals(result, obs.getValues().get(0));
        assertEquals(result, obs.getHumanReadableValues().get(0));
    }

    private static final String NCD_PAYLOAD = "{\n"
            + "  \"gothomis_response\" : {\n"
            + "    \"id\" : 8787,\n"
            + "    \"referral_no\" : \"61220\",\n"
            + "    \"received_feedback_payload\" : {\n"
            + "      \"outcomes\" : [ {\n"
            + "        \"ncdFinalDiagnosis\" : {\n"
            + "          \"diabeticClient\" : false,\n"
            + "          \"hypertensiveClient\" : true\n"
            + "        }\n"
            + "      }, {\n"
            + "        \"ncdFinalDiagnosis\" : {\n"
            + "          \"diabeticClient\" : true,\n"
            + "          \"hypertensiveClient\" : false\n"
            + "        }\n"
            + "      } ],\n"
            + "      \"referralNo\" : \"61220\",\n"
            + "      \"prescriptions\" : [ {\n"
            + "        \"dispensed\" : false,\n"
            + "        \"prescriptionName\" : \"NO Prescription GIVEN\"\n"
            + "      } ],\n"
            + "      \"servicesProvided\" : [ {\n"
            + "        \"hfrCode\" : \"102557-6\",\n"
            + "        \"serviceCode\" : \"REFERRAL\",\n"
            + "        \"serviceName\" : \"Referral Services\",\n"
            + "        \"facilityName\" : \"KIA - Dispensary\"\n"
            + "      } ],\n"
            + "      \"referralFeedbackDate\" : \"2026-06-24\"\n"
            + "    },\n"
            + "    \"received_date\" : \"2026-06-24 10:54:48.471757+03\",\n"
            + "    \"processed_date\" : null\n"
            + "  },\n"
            + "  \"event_metadata\" : {\n"
            + "    \"locationid\" : \"fbdd93f1-2045-4744-ae38-133f78a049c0\",\n"
            + "    \"providerid\" : \"markchw\",\n"
            + "    \"team\" : \"Kia - 102557-6\",\n"
            + "    \"baseentityid\" : \"7aa6c349-6dcd-45e8-bf05-f53cec4ef6aa\",\n"
            + "    \"teamid\" : \"8b0ad916-115b-410c-9dd2-0b9fc00ca84f\",\n"
            + "    \"taskid\" : \"fe6b0474-20d4-4c18-b234-cc94272fc390\"\n"
            + "  }\n"
            + "}";

    private static final String NO_NCD_PAYLOAD = "{\n"
            + "  \"gothomis_response\" : {\n"
            + "    \"id\" : 8787,\n"
            + "    \"referral_no\" : \"61220\",\n"
            + "    \"received_feedback_payload\" : {\n"
            + "      \"outcomes\" : [],\n"
            + "      \"referralNo\" : \"61220\",\n"
            + "      \"servicesProvided\" : [],\n"
            + "      \"prescriptions\" : [],\n"
            + "      \"referralFeedbackDate\" : \"2026-06-24\"\n"
            + "    },\n"
            + "    \"received_date\" : \"2026-06-24 10:54:48.471757+03\",\n"
            + "    \"processed_date\" : null\n"
            + "  },\n"
            + "  \"event_metadata\" : {\n"
            + "    \"locationid\" : \"fbdd93f1-2045-4744-ae38-133f78a049c0\",\n"
            + "    \"providerid\" : \"markchw\",\n"
            + "    \"team\" : \"Kia - 102557-6\",\n"
            + "    \"baseentityid\" : \"7aa6c349-6dcd-45e8-bf05-f53cec4ef6aa\",\n"
            + "    \"teamid\" : \"8b0ad916-115b-410c-9dd2-0b9fc00ca84f\",\n"
            + "    \"taskid\" : \"fe6b0474-20d4-4c18-b234-cc94272fc390\"\n"
            + "  }\n"
            + "}";
}
