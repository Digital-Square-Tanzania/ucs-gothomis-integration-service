package com.abt.util;

import com.abt.domain.ClientEvents;
import com.abt.domain.CommunityLinkageRequest;
import com.abt.domain.Event;
import com.abt.domain.Obs;
import com.abt.integration.model.ChwMetadata;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenSrpServiceCommunityLinkageTest {

    @Test
    void buildsCommunityLinkageClientAndEvent() {
        CommunityLinkageRequest request = buildRequest();
        ChwMetadata chwMetadata = new ChwMetadata("tintu", "Team A", "team-uuid-1", "location-uuid-1", "Village");
        Event event = OpenSrpService.buildCommunityLinkageEvent(request, "base-123", "1223141", chwMetadata);

        assertEquals("Community Linkage", event.getEventType());
        assertEquals("base-123", event.getBaseEntityId());
        assertEquals("tintu", event.getProviderId());
        assertEquals("Team A", event.getTeam());
        assertEquals("team-uuid-1", event.getTeamId());
        assertEquals("location-uuid-1", event.getLocationId());
        assertEquals("string", event.getDetails().get("reason"));
        assertEquals("1223141", event.getIdentifiers().get("opensrp_id"));
        assertEquals("1223141", event.getIdentifiers().get("UCS|MRN"));
        Set<String> eventObs = event.getObs().stream().map(obs -> obs.getFieldCode()).collect(Collectors.toSet());
        assertTrue(eventObs.contains("reason"));
        assertTrue(eventObs.contains("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        assertTrue(eventObs.contains("163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        assertNotNull(event.getEventDate());
        assertNotNull(event.getDateCreated());
        assertEquals("Married", maritalStatusValue(event));
    }

    @Test
    void buildsRegistrationPayloadForNewClient() {
        CommunityLinkageRequest request = buildRequest();
        ChwMetadata chwMetadata = new ChwMetadata("tintu", "Team A", "team-uuid-1", "location-uuid-1", "Village");

        ClientEvents clientEvents = OpenSrpService.buildCommunityLinkageRegistrationPayload(
                request, "base-123", "1223141", chwMetadata
        );

        assertEquals(2, clientEvents.getClients().size());
        assertEquals(3, clientEvents.getEvents().size());
        assertEquals(3, clientEvents.getNoOfEvents());
        assertEquals("1223141_family", clientEvents.getClients().get(0).getIdentifiers().get("opensrp_id"));
        assertEquals("1223141", clientEvents.getClients().get(1).getIdentifiers().get("opensrp_id"));
        assertEquals("Married", clientEvents.getClients().get(1).getAttributes().get("marital_status"));
        assertEquals("base-123", clientEvents.getClients().get(0).getRelationships().get("family_head").get(0));
        assertEquals(1, clientEvents.getClients().get(0).getAddresses().size());
        assertEquals("Village", clientEvents.getClients().get(0).getAddresses().get(0).getCityVillage());
        assertEquals(clientEvents.getClients().get(0).getBaseEntityId(),
                clientEvents.getClients().get(1).getRelationships().get("family").get(0));
        assertEquals(
                Set.of("Family Registration", "Family Member Registration", "Community Linkage"),
                clientEvents.getEvents().stream().map(Event::getEventType).collect(Collectors.toSet())
        );
    }

    @Test
    void skipsMaritalStatusWhenUnknown() {
        CommunityLinkageRequest request = buildRequest();
        request.setMaritalStatus("UNKNOWN");
        ChwMetadata chwMetadata = new ChwMetadata("tintu", "Team A", "team-uuid-1", "location-uuid-1", "Village");

        ClientEvents clientEvents = OpenSrpService.buildCommunityLinkageRegistrationPayload(
                request, "base-123", "1223141", chwMetadata
        );
        Event event = OpenSrpService.buildCommunityLinkageEvent(request, "base-123", "1223141", chwMetadata);

        assertEquals(null, clientEvents.getClients().get(1).getAttributes().get("marital_status"));
        assertEquals(null, maritalStatusValue(event));
        assertEquals(
                null,
                clientEvents.getEvents().stream()
                        .filter(candidate -> "Family Member Registration".equals(candidate.getEventType()))
                        .findFirst()
                        .map(OpenSrpServiceCommunityLinkageTest::maritalStatusValue)
                        .orElse(null)
        );
    }

    private static CommunityLinkageRequest buildRequest() {
        CommunityLinkageRequest request = new CommunityLinkageRequest();
        CommunityLinkageRequest.Identifiers identifiers = new CommunityLinkageRequest.Identifiers();
        identifiers.setTypeOfIdentifier("UCS|MRN");
        identifiers.setValue("1223141");
        request.setIdentifiers(identifiers);
        request.setFirstName("Tintu");
        request.setMiddleName("Nyati");
        request.setLastName("Maeda");
        request.setBirthDate("1960-03-06");
        request.setSex("MALE");
        request.setMobileNumber("0763810222");
        request.setMaritalStatus("MARRIED");
        request.setChwUsername("tintu");
        request.setReason("string");
        return request;
    }

    private static String maritalStatusValue(Event event) {
        return event.getObs().stream()
                .filter(obs -> "marital_status".equals(obs.getFieldCode()))
                .findFirst()
                .map(Obs::getValue)
                .map(Objects::toString)
                .orElse(null);
    }
}
