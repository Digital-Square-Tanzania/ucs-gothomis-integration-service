package com.abt.actors;

import com.abt.domain.Event;
import com.abt.domain.EventRequest;
import com.abt.domain.ReferralResponse;
import com.abt.util.OpenSrpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.abt.util.OpenSrpService.sendDataToDestination;

public class SendResponseProcessor {
    private final static Logger log = LoggerFactory.getLogger(SendResponseProcessor.class);
    public String sendResponse(ReferralResponse referralResponse, String url, String username, String password) {
        try {
            List<Event> events = new ArrayList<>();
            Event referralResponseEvent =
                    OpenSrpService.getReferralResponseEvent(referralResponse);
            Event pregnancyConfirmationEvent =
                    OpenSrpService.getPregnancyConfirmationEvent(referralResponse);


            events.add(referralResponseEvent);
            if (pregnancyConfirmationEvent != null) {
                events.add(pregnancyConfirmationEvent);
            }

            return sendDataToDestination(new EventRequest(events), url, username, password);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Internal Error while processing the payload";
        }
    }
}
