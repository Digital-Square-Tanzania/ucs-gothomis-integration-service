package com.abt;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import com.abt.domain.ReferralResponse;
import com.abt.util.CustomJacksonSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.Directives.complete;

public class UcsGothomisIntegrationRoutes {

    //Routes Class
    private final static Logger log = LoggerFactory.getLogger(UcsGothomisIntegrationRoutes.class);
    private final ActorRef<UcsGothomisIntegrationRegistry.Command> gothomisIntegrationActor;
    private final Duration askTimeout;
    private final Scheduler scheduler;
    private final String url;
    private final String username;
    private final String password;

    public UcsGothomisIntegrationRoutes(ActorSystem<?> system, ActorRef<UcsGothomisIntegrationRegistry.Command> gothomisIntegrationActor){
        this.gothomisIntegrationActor = gothomisIntegrationActor;
        scheduler = system.scheduler();
        askTimeout = system.settings().config().getDuration("integration-service.routes.ask-timeout");
        url = system.settings().config().getString("integration-service.destination.url");
        username = system.settings().config().getString("integration-service.destination.username");
        password = system.settings().config().getString("integration-service.destination.password");
    }

    private CompletionStage<UcsGothomisIntegrationRegistry.ActionPerformed> sendResponse(ReferralResponse response) {
        return AskPattern.ask(gothomisIntegrationActor, ref -> new UcsGothomisIntegrationRegistry.SendReferralResponse(response, url, username, password, ref), askTimeout, scheduler);
    }

    //Add rejection route

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */
    public Route referralResponseRoutes() {
        return pathPrefix("send", () ->
                concat(
                        //#send-results
                        pathSuffix("-results", () ->
                                concat(
                                        post(() ->
                                                entity(
                                                        CustomJacksonSupport.customJacksonUnmarshaller(ReferralResponse.class),
                                                        result ->
                                                                onSuccess(sendResponse(result), performed -> {
                                                                    log.info("Sent Referral Results: {}", performed.description());
                                                                    if (performed.description().toLowerCase().contains("error")) {
                                                                        return complete(StatusCodes.BAD_REQUEST, performed, Jackson.marshaller());
                                                                    } else {
                                                                        return complete(StatusCodes.OK, performed, Jackson.marshaller());
                                                                    }
                                                                })
                                                )
                                        )
                                )
                        )
                )
        );
    }

}
