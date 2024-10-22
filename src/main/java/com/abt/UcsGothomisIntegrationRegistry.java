package com.abt;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.abt.actors.SendResponseProcessor;
import com.abt.domain.ReferralResponse;


public class UcsGothomisIntegrationRegistry extends AbstractBehavior<UcsGothomisIntegrationRegistry.Command> {

    private UcsGothomisIntegrationRegistry(ActorContext<Command> context){
        super(context);
    }

    public static Behavior<Command> create(){
        return Behaviors.setup(UcsGothomisIntegrationRegistry::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(SendReferralResponse.class, this::onSendRefferralResponse)
                .build();
    }

    private Behavior<UcsGothomisIntegrationRegistry.Command> onSendRefferralResponse(SendReferralResponse command){
        String response = new SendResponseProcessor().sendResponse(command.referralResponse, command.url, command.username, command.password);
        command.replyTo().tell(new UcsGothomisIntegrationRegistry.ActionPerformed(String.format(response)));
        return this;
    }

    sealed interface Command {
    }

    public final static record SendReferralResponse(ReferralResponse referralResponse,
                                                    String url, String username, String password,
                                                    ActorRef<UcsGothomisIntegrationRegistry.ActionPerformed> replyTo) implements UcsGothomisIntegrationRegistry.Command {
    }

    public final static record ActionPerformed(String description) implements UcsGothomisIntegrationRegistry.Command {
    }

}