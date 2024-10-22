package com.abt.actors;

import com.abt.domain.Event;
import com.abt.domain.EventRequest;
import com.abt.domain.LabResult;
import com.abt.util.OpenSrpService;

import com.abt.domain.ReferralResponse;

import java.util.ArrayList;
import java.util.List;

import static com.abt.util.OpenSrpService.sendDataToDestination;

public class SendResponseProcessor {
    public String sendResponse(ReferralResponse referralResponse, String url, String username, String password) {
        try {
            /**
             *
             *  1. Handle the referral feedback event creating the event "Referral Feedback" as a result
             *  2. Capture the referral task to be updated
             *
             */

            Event referralResponseEvent = OpenSrpService.getReferralResponseEvent(referralResponse);

            List<Event> events = new ArrayList<>();
            events.add(referralResponseEvent);

            //Task creation implementation


            return sendDataToDestination(new EventRequest(events), url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return "Internal Error while processing the payload";
        }
    }
}
