package com.abt.util;


import akka.http.javadsl.model.DateTime;
import com.abt.UcsGothomisIntegrationRoutes;
import com.abt.domain.Client;
import com.abt.domain.ClientEvents;
import com.abt.domain.CommunityLinkageRequest;
import com.abt.domain.Event;
import com.abt.domain.EventRequest;
import com.abt.domain.Obs;
import com.abt.domain.ReferralResponse;
import com.abt.integration.model.ChwMetadata;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class for OpenSRP operations.
 */
public class OpenSrpService {

    private final static Logger log = LoggerFactory.getLogger(OpenSrpService.class);

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
    public static Event getReferralResponseEvent(ReferralResponse referralResponse) {
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
     * Creates the Pregnancy Confirmation events form the response
     * object
     *
     * @param referralResponse the referral response object
     * @return Pregnancy Confirmation Event
     */
    public static Event getPregnancyConfirmationEvent(ReferralResponse referralResponse) {
        Event pregnancyConfirmationStatusEvent = new Event();

        ReferralResponse.GothomisResponse response =
                referralResponse.getGothomisResponse();

        ReferralResponse.ResponseMetadata responseMetadata =
                response.getResponseMetadata();

        List<ReferralResponse.Outcomes> outcomes =
                responseMetadata.getOutcomes();

        for (ReferralResponse.Outcomes outcome : outcomes) {
            if (outcome.getPregnancyConfirmation() != null) {
                ReferralResponse.EventMetadata eventMetadata =
                        referralResponse.getEventMetadata();

                setMetaData(pregnancyConfirmationStatusEvent, eventMetadata);
                pregnancyConfirmationStatusEvent.setBaseEntityId(eventMetadata.getBaseEntityId());

                pregnancyConfirmationStatusEvent.setEventType("Pregnancy " +
                        "Confirmation");
                List<Obs> obs = new ArrayList<>();

                obs.add(generateObservation("pregnancy_confirmation_status",
                        "pregnancy_confirmation_status",
                        new ArrayList<>(Collections.singletonList(outcome.getPregnancyConfirmation().getStatus())),
                        List.of(), false));

                if (StringUtils.isNotBlank(outcome.getPregnancyConfirmation().getEdd())) {
                    obs.add(generateObservation("edd", "edd",
                            new ArrayList<>(Collections.singletonList(outcome.getPregnancyConfirmation().getEdd())), List.of(), false));
                }


                if (StringUtils.isNotBlank(outcome.getPregnancyConfirmation().getLnmp())) {
                    try {
                        obs.add(generateObservation("gest_age", "gest_age",
                                new ArrayList<>(Collections.singletonList(
                                        getDifferenceInWeeks(outcome.getPregnancyConfirmation().getLnmp())
                                )), List.of(), false));
                    } catch (Exception e){
                        log.error(e.getMessage());
                    }

                    obs.add(generateObservation("last_menstrual_period",
                            "last_menstrual_period",
                            new ArrayList<>(Collections.singletonList(outcome.getPregnancyConfirmation().getLnmp())), List.of(), false));
                }

                if (outcome.getPregnancyConfirmation().getPara() != null) {
                    obs.add(generateObservation("parity", "parity",
                            new ArrayList<>(Collections.singletonList(eventMetadata.getTaskId())), List.of(), false));
                }

                if (outcome.getPregnancyConfirmation().getGravida() != null) {
                    obs.add(generateObservation("gravida", "gravida",
                            new ArrayList<>(Collections.singletonList(eventMetadata.getTaskId())), List.of(), false));
                }


                pregnancyConfirmationStatusEvent.setObs(obs);
                return pregnancyConfirmationStatusEvent;
            }
        }
        return null;
    }

    /**
     * Generate a list of Obs from the eventMetadata object
     * @param responseMetadata
     * @return obs, list of obs to add to the event
     */
    private static List<Obs> getReferralObs(ReferralResponse.ResponseMetadata responseMetadata) {
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

            if (prescription.isDespensed()) {
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

    private static void setMetaData(Event event, ChwMetadata chwMetadata) {
        event.setLocationId(chwMetadata.locationId());
        event.setProviderId(chwMetadata.providerId());
        event.setTeamId(chwMetadata.teamId());
        event.setTeam(chwMetadata.team());
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

    public static Client buildCommunityLinkageFamilyClient(CommunityLinkageRequest request,
                                                           String familyBaseEntityId,
                                                           String uniqueId,
                                                           String clientBaseEntityId) {
        Client familyClient = new Client(familyBaseEntityId);
        String familyName = StringUtils.firstNonBlank(request.getLastName(), request.getFirstName(), uniqueId);
        familyClient.setFirstName(familyName);
        familyClient.setLastName("Family");
        familyClient.setBirthdate(new Date(0));
        familyClient.setBirthdateApprox(false);
        familyClient.setDeathdateApprox(false);
        familyClient.setGender(normalizeGender(request.getSex()));
        familyClient.setType("Client");
        familyClient.setId(UUID.randomUUID().toString());
        familyClient.setDateCreated(new Date());
        familyClient.setClientApplicationVersion(clientApplicationVersion);
        familyClient.setClientDatabaseVersion(clientDatabaseVersion);
        familyClient.setAttributes(new HashMap<>());

        Map<String, List<String>> relationships = new HashMap<>();
        relationships.put("family_head", Collections.singletonList(clientBaseEntityId));
        relationships.put("primary_caregiver", Collections.singletonList(clientBaseEntityId));
        familyClient.setRelationships(relationships);

        Map<String, String> identifiers = new HashMap<>();
        identifiers.put("opensrp_id", uniqueId + "_family");
        familyClient.setIdentifiers(identifiers);

        return familyClient;
    }

    public static Client buildCommunityLinkageClient(CommunityLinkageRequest request,
                                                     String baseEntityId,
                                                     String uniqueId) {
        Client client = new Client(baseEntityId);
        client.setFirstName(request.getFirstName());
        client.setMiddleName(request.getMiddleName());
        client.setLastName(request.getLastName());
        client.setGender(normalizeGender(request.getSex()));
        client.setBirthdate(parseBirthdate(request.getBirthDate()));
        client.setBirthdateApprox(false);
        client.setDeathdateApprox(false);
        client.setType("Client");
        client.setId(UUID.randomUUID().toString());
        client.setDateCreated(new Date());
        client.setClientApplicationVersion(clientApplicationVersion);
        client.setClientDatabaseVersion(clientDatabaseVersion);

        Map<String, String> identifiers = new HashMap<>();
        identifiers.put("opensrp_id", uniqueId);
        if (request.getIdentifiers() != null && StringUtils.isNotBlank(request.getIdentifiers().getTypeOfIdentifier())) {
            identifiers.put(request.getIdentifiers().getTypeOfIdentifier(), request.getIdentifiers().getValue());
        }
        client.setIdentifiers(identifiers);

        Map<String, Object> attributes = new HashMap<>();
        putIfNotBlank(attributes, "mobile_number", request.getMobileNumber());
        putIfNotBlank(attributes, "marital_status", request.getMaritalStatus());
        putIfNotBlank(attributes, "chw_username", request.getChwUsername());
        putIfNotBlank(attributes, "reason", request.getReason());
        client.setAttributes(attributes);

        return client;
    }

    public static Event buildCommunityLinkageFamilyRegistrationEvent(String familyBaseEntityId,
                                                                     ChwMetadata chwMetadata) {
        Event familyRegistrationEvent = new Event();
        familyRegistrationEvent.setBaseEntityId(familyBaseEntityId);
        familyRegistrationEvent.setEventType("Family Registration");
        familyRegistrationEvent.setEntityType("ec_independent_client");
        setMetaData(familyRegistrationEvent, chwMetadata);
        familyRegistrationEvent.addObs(new Obs("formsubmissionField", "text",
                "last_interacted_with", "",
                Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}),
                null, null, "last_interacted_with"));
        return familyRegistrationEvent;
    }

    public static Event buildCommunityLinkageFamilyMemberRegistrationEvent(CommunityLinkageRequest request,
                                                                           String baseEntityId,
                                                                           ChwMetadata chwMetadata) {
        Event familyMemberRegistrationEvent = new Event();
        familyMemberRegistrationEvent.setBaseEntityId(baseEntityId);
        familyMemberRegistrationEvent.setEventType("Family Member Registration");
        familyMemberRegistrationEvent.setEntityType("ec_independent_client");
        setMetaData(familyMemberRegistrationEvent, chwMetadata);
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField",
                "text", "id_avail", "", Arrays.asList(new Object[]{"None"}),
                null, null, "id_avail"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField",
                "text", "leader", "", Arrays.asList(new Object[]{"None"}),
                null, null, "leader"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField",
                "text", "last_interacted_with", "",
                Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}),
                null, null, "last_interacted_with"));
        putObsIfNotBlank(familyMemberRegistrationEvent.getObs(), "surname",
                request.getLastName(), "surname");
        putObsIfNotBlank(familyMemberRegistrationEvent.getObs(), "phone_number",
                request.getMobileNumber(), "phone_number");
        putObsIfNotBlank(familyMemberRegistrationEvent.getObs(), "marital_status",
                request.getMaritalStatus(), "marital_status");
        familyMemberRegistrationEvent.addObs(new Obs("concept", "text",
                "data_source", "", Arrays.asList(new Object[]{"community_linkage"}),
                null, null, "data_source"));
        return familyMemberRegistrationEvent;
    }

    public static ClientEvents buildCommunityLinkageRegistrationPayload(CommunityLinkageRequest request,
                                                                        String baseEntityId,
                                                                        String uniqueId,
                                                                        ChwMetadata chwMetadata) {
        String familyBaseEntityId = UUID.randomUUID().toString();
        Client familyClient = buildCommunityLinkageFamilyClient(request, familyBaseEntityId, uniqueId, baseEntityId);
        Client client = buildCommunityLinkageClient(request, baseEntityId, uniqueId);

        Map<String, List<String>> clientRelationships = new HashMap<>();
        clientRelationships.put("family", Collections.singletonList(familyBaseEntityId));
        client.setRelationships(clientRelationships);

        List<Client> clients = new ArrayList<>();
        clients.add(familyClient);
        clients.add(client);

        List<Event> events = new ArrayList<>();
        events.add(buildCommunityLinkageFamilyRegistrationEvent(familyBaseEntityId, chwMetadata));
        events.add(buildCommunityLinkageFamilyMemberRegistrationEvent(request, baseEntityId, chwMetadata));
        events.add(buildCommunityLinkageEvent(request, baseEntityId, uniqueId, chwMetadata));

        ClientEvents clientEvents = new ClientEvents();
        clientEvents.setClients(clients);
        clientEvents.setEvents(events);
        clientEvents.setNoOfEvents(events.size());
        return clientEvents;
    }

    public static Event buildCommunityLinkageEvent(CommunityLinkageRequest request,
                                                   String baseEntityId,
                                                   String uniqueId,
                                                   ChwMetadata chwMetadata) {
        Event event = new Event();
        event.setBaseEntityId(baseEntityId);
        event.setEventType("Community Linkage");
        event.setEntityType("community_linkage");
        setMetaData(event, chwMetadata);
        event.addIdentifier("opensrp_id", uniqueId);
        if (request.getIdentifiers() != null && StringUtils.isNotBlank(request.getIdentifiers().getTypeOfIdentifier())
                && StringUtils.isNotBlank(request.getIdentifiers().getValue())) {
            event.addIdentifier(request.getIdentifiers().getTypeOfIdentifier(), request.getIdentifiers().getValue());
        }
        if (StringUtils.isNotBlank(request.getReason())) {
            event.addDetails("reason", request.getReason());
        }


        List<Obs> obs = event.getObs();
        putObsIfNotBlank(obs, "reason", request.getReason(), "reason");
        putObsIfNotBlank(obs, "identifier_type", request.getIdentifiers() == null ? null : request.getIdentifiers().getTypeOfIdentifier(), "identifier_type");
        putObsIfNotBlank(obs, "identifier_value", request.getIdentifiers() == null ? null : request.getIdentifiers().getValue(), "identifier_value");
        putObsIfNotBlank(obs, "mobile_number", request.getMobileNumber(), "mobile_number");
        putObsIfNotBlank(obs, "marital_status", request.getMaritalStatus(), "marital_status");

        return event;
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
        return sendPayloadToDestination(events, mUrl, username, password);
    }

    public static String sendDataToDestination(ClientEvents clientEvents, String mUrl, String username, String password) {
        return sendPayloadToDestination(clientEvents, mUrl, username, password);
    }

    private static String sendPayloadToDestination(Object payload, String mUrl, String username, String password) {
        String response;
        try {
            URL url = new URL(mUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            configureBasicAuthHeader(username, password, conn);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = buildPayloadGson().toJson(payload).getBytes(StandardCharsets.UTF_8);
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

    private static Gson buildPayloadGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
                .create();
    }

    private static void putIfNotBlank(Map<String, Object> target, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            target.put(key, value);
        }
    }

    private static void putObsIfNotBlank(List<Obs> obs,
                                         String fieldCode,
                                         String value,
                                         String formSubmissionField) {
        if (StringUtils.isNotBlank(value)) {
            obs.add(new Obs("concept", "text", fieldCode, "",
                    new ArrayList<>(Collections.singletonList(value)),
                    List.of(), null, formSubmissionField));
        }
    }

    private static Date parseBirthdate(String birthDate) {
        if (StringUtils.isBlank(birthDate)) {
            return null;
        }
        return Date.from(java.time.LocalDate.parse(birthDate).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static String normalizeGender(String sex) {
        if (StringUtils.isBlank(sex)) {
            return null;
        }
        if ("MALE".equalsIgnoreCase(sex) || "M".equalsIgnoreCase(sex)) {
            return "Male";
        }
        if ("FEMALE".equalsIgnoreCase(sex) || "F".equalsIgnoreCase(sex)) {
            return "Female";
        }
        return sex;
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

    public static long getDifferenceInWeeks(String startDateStr) {
        // Define the date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Parse the first date string into a LocalDate
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the difference in weeks
        return ChronoUnit.WEEKS.between(startDate, currentDate);
    }
}
