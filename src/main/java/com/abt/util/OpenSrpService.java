package com.abt.util;


import akka.http.javadsl.model.DateTime;
import com.abt.domain.ReferralResponse;

import com.abt.domain.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class for OpenSRP operations.
 */
public class OpenSrpService {

    private static final int clientDatabaseVersion = 17;
    private static final int clientApplicationVersion = 2;
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Creates and returns an observation for the start event.
     *
     * @return Obs object for the start event.
     */
    private static Obs getStartOb() {
        return new Obs("concept", "start",
                "163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "",
                Arrays.asList(new Object[]{new Date()}), null, null, "start");
    }

    /**
     * Creates and returns an observation for the end event.
     *
     * @return Obs object for the end event.
     */
    private static Obs getEndOb() {
        return new Obs("concept", "end",
                "163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "",
                Arrays.asList(new Object[]{new Date()}), null, null, "end");
    }


    /**
     * Creates the referral response events form the response object
     * 
     * @param referralResponse the referral response object 
     * @return Referral Event and subsequent events
     */
    public static Event getReferralResponseEvent(ReferralResponse referralResponse){
        Event referralEvent = new Event();

        ReferralResponse.GothomisResponse response = referralResponse.getGothomisResponse();

        ReferralResponse.ResponseMetadata responseMetadata = response.getResponseMetadata();
        ReferralResponse.EventMetadata eventMetadata = referralResponse.getEventMetadata();

        setMetaData(referralEvent, eventMetadata);
        referralEvent.setBaseEntityId(eventMetadata.getBaseEntityId());

        referralEvent.setEventType("Close Referral");
        List<Obs> referralObs = getReferralObs(responseMetadata);

        referralObs.add(generateObservation("referral_task_previous_status", "referral_task_previous_status", new ArrayList<>(Collections.singletonList("Ready")), List.of(), false));
        referralObs.add(generateObservation("referral_task_previous_business_status", "referral_task_previous_business_status", new ArrayList<>(Collections.singletonList("Referred")), List.of(), false));
        referralObs.add(generateObservation("referral_task", "referral_task", new ArrayList<>(Collections.singletonList(eventMetadata.getTaskId())), List.of(), false));

        referralEvent.setObs(referralObs);

        return referralEvent;
    }

    /**
     * Generate a list of Obs from the eventMetadata object
     * @param responseMetadata
     * @return obs, list of obs to add to the event
     */
    private static List<Obs> getReferralObs(ReferralResponse.ResponseMetadata responseMetadata){
        List<Obs> obs = new ArrayList<>();

        obs.add(generateObservation("referralNo", "referralNo", new ArrayList<>(Collections.singletonList(responseMetadata.getRefferralNo())), null));
        obs.add(generateObservation("referralFeedbackDate", "referralFeedbackDate", new ArrayList<>(Collections.singletonList(responseMetadata.getReferralFeedbackDate())), null));

        // Service provided observation
        List<Object> service_codes = new ArrayList<>();
        List<Object> service_names = new ArrayList<>();
        List<Object> hfrCodes = new ArrayList<>();
        for (ReferralResponse.ServicesProvided servicesProvided : responseMetadata.getServicesProvided()){
            service_codes.add(servicesProvided.getServiceCode());
            service_names.add(servicesProvided.getServiceName());
            hfrCodes.add(servicesProvided.getHfrCode());
        }
        Obs servicesProvidedOb = generateObservation("servicesProvided", "servicesProvided", service_codes, service_names);
        servicesProvidedOb.setComments(hfrCodes.toString());
        obs.add(servicesProvidedOb);

        //Prescriptions observation
        List<Object> prescription_codes = new ArrayList<>();
        List<Object> prescription_names = new ArrayList<>();
        List<Boolean> prescription_dispensed = new ArrayList<Boolean>();

        List<Object> dispencedPrescriptionCodes = new ArrayList<>();
        List<Object> dispencedPrescriptionNames = new ArrayList<>();

        for (ReferralResponse.Prescriptions prescription : responseMetadata.getPrescriptions()){
            prescription_codes.add(prescription.getPrescriptionCode());
            prescription_names.add(prescription.getPrescriptionName());
            prescription_dispensed.add(prescription.isDespensed());

            if (prescription.isDespensed()){
                dispencedPrescriptionCodes.add(prescription.getPrescriptionCode());
                dispencedPrescriptionNames.add(prescription.getPrescriptionName());
            }

        }
        Obs prescriptionsObservation = generateObservation("prescriptions", "prescriptions", prescription_codes, prescription_names);
        prescriptionsObservation.setComments(prescription_dispensed.toString());
        obs.add(prescriptionsObservation);

        //Dispensed medication observation
        Obs dispencedPrescriptionObservation = generateObservation("dispensedMedication", "dispencedMedication", dispencedPrescriptionCodes, dispencedPrescriptionNames);
        obs.add(dispencedPrescriptionObservation);

        //Outcome observation
        Obs outcomeObservation = generateObservation("outcome", "outcome", new ArrayList<>(Collections.singletonList(responseMetadata.outcomeToJsonString())), new ArrayList<>(Collections.singletonList(responseMetadata.outcomeToJsonString())));
        obs.add(outcomeObservation);

        return obs;
    }

    private static Obs generateObservation(String fieldCode, String formSubmissionField, List<Object> value, List<Object> humanReadableValues){
        return new Obs(
                "concept",
                "text",
                fieldCode,
                "",
                value,
                humanReadableValues,
                null,
                formSubmissionField);
    }

    private static Obs generateObservation(String fieldCode, String formSubmissionField, List<Object> value, List<Object> humanReadableValues, boolean saveAsArray){
        return new Obs(
                "concept",
                "text",
                fieldCode,
                "",
                value,
                humanReadableValues,
                null,
                formSubmissionField,
                saveAsArray);
    }

    /**
     * Set Event Metadata
     *
     * @param event              created Event
     * @param eventMetadata Object
     */
    private static void setMetaData(Event event, ReferralResponse.EventMetadata eventMetadata) {
        event.setLocationId(eventMetadata.getLocationId());
        event.setProviderId(eventMetadata.getProviderId());
        event.setTeamId(eventMetadata.getTeamId());
        event.setTeam(eventMetadata.getTeam());
        event.setType("Event");
        event.setFormSubmissionId(UUID.randomUUID().toString());
        event.setEventDate(new Date());
        event.setDateCreated(new Date());
        event.addObs(OpenSrpService.getStartOb());
        event.addObs(OpenSrpService.getEndOb());
        event.setClientApplicationVersion(clientApplicationVersion);
        event.setClientDatabaseVersion(clientDatabaseVersion);
        event.setDuration(0);
        event.setIdentifiers(new HashMap<>());
    }

    private static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static List<Integer> extractIntegers(String str) {
        List<Integer> integers = new ArrayList<>();
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            integers.add(Integer.parseInt(matcher.group()));
        }

        return integers;
    }


    public static String sendDataToDestination(EventRequest events, String mUrl, String username, String password) {
        String response = "";
        try {
            URL url = new URL(mUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            configureBasicAuthHeader(username, password, conn);

            try (OutputStream os = conn.getOutputStream()) {

                Gson gson
                        = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
                        .create();

                byte[] input =
                        gson.toJson(events).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = conn.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) { // success
                System.out.println("POST was successful.");
                response = "sending successful";
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                System.out.println("POST request failed.");
                response = "Authentication Error: Incorrect Username or password";
            } else {
                System.out.println("POST request failed.");
                response = "Error: Sending data to UCS failed";
            }

            conn.disconnect();
        } catch (Exception e) {
            response = "Error: " + e.getMessage();

        }
        return response;
    }

    private static Date parseDate(String dateString) throws ParseException {
        Date rejectionDateTimeObj;
        if (dateString.contains("T")) {
            rejectionDateTimeObj = inputFormat.parse(dateString);
        } else {
            rejectionDateTimeObj = inputFormat2.parse(dateString);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(rejectionDateTimeObj);
        return calendar.getTime();
    }

    public static void configureBasicAuthHeader(String username,
                                                String password,
                                                HttpURLConnection conn) {
        if (
                username != null &&
                        !username.isEmpty() &&
                        password != null &&
                        !password.isEmpty()
        ) {
            String auth = username + ":" + password;
            byte[] encodedAuth =
                    Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);

            conn.setRequestProperty("Authorization", authHeader);
        }
    }
}
