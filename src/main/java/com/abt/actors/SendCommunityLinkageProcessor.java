package com.abt.actors;

import com.abt.domain.ClientEvents;
import com.abt.domain.CommunityLinkageRequest;
import com.abt.domain.Event;
import com.abt.domain.EventRequest;
import com.abt.integration.config.PostgresConnectionFactory;
import com.abt.integration.db.CommunityLinkageRepository;
import com.abt.integration.model.ChwMetadata;
import com.abt.integration.model.ClientLookupResult;
import com.abt.util.OpenSrpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static com.abt.util.OpenSrpService.sendDataToDestination;

public class SendCommunityLinkageProcessor {
    private final static Logger log = LoggerFactory.getLogger(SendCommunityLinkageProcessor.class);

    public String sendCommunityLinkage(CommunityLinkageRequest request, String url, String username, String password) {
        PostgresConnectionFactory connectionFactory = new PostgresConnectionFactory();
        CommunityLinkageRepository repository = new CommunityLinkageRepository(connectionFactory.schema());

        try (Connection connection = connectionFactory.openConnection()) {
            log.info(SendCommunityLinkageProcessor.class.getSimpleName(),"DB Connection Established");
            String identifierValue = request.getIdentifiers() == null ? null : request.getIdentifiers().getValue();
            if (identifierValue == null || identifierValue.isBlank()) {
                return "Internal Error while processing the payload: identifiers.value is required";
            }

            ChwMetadata chwMetadata = repository.findChwMetadata(connection, request.getChwUsername())
                    .orElseThrow(() -> new IllegalStateException("Unable to resolve CHW metadata for chwUsername=" + request.getChwUsername()));

            ClientLookupResult lookupResult = repository.findClientByUniqueId(connection, identifierValue).orElse(null);
            String baseEntityId = lookupResult != null && lookupResult.baseEntityId() != null
                    ? lookupResult.baseEntityId()
                    : UUID.randomUUID().toString();
            String uniqueId = lookupResult != null && lookupResult.uniqueId() != null
                    ? lookupResult.uniqueId()
                    : identifierValue;

            Event communityLinkageEvent = OpenSrpService.buildCommunityLinkageEvent(request, baseEntityId, uniqueId, chwMetadata);

            if (lookupResult == null) {
                ClientEvents clientEvents = OpenSrpService.buildCommunityLinkageRegistrationPayload(
                        request, baseEntityId, uniqueId, chwMetadata
                );
                return sendDataToDestination(clientEvents, url, username, password);
            }

            return sendDataToDestination(new EventRequest(List.of(communityLinkageEvent)), url, username, password);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}
