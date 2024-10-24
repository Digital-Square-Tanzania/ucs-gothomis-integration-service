package com.abt.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ReferralResponse {

    @JsonProperty("gothomis_response")
    private GothomisResponse gothomisResponse;

    @JsonProperty("event_metadata")
    private EventMetadata eventMetadata;

    public void setEventMetadata(EventMetadata eventMetadata) {
        this.eventMetadata = eventMetadata;
    }

    public void setGothomisResponse(GothomisResponse gothomisResponse) {
        this.gothomisResponse = gothomisResponse;
    }

    public EventMetadata getEventMetadata() {
        return eventMetadata;
    }

    public GothomisResponse getGothomisResponse() {
        return gothomisResponse;
    }

    public static class GothomisResponse {
        @JsonProperty("id")
        private int id;

        @JsonProperty("referral_no")
        private String referralNo;

        @JsonProperty("received_date")
        private String receivedDate;

        @JsonProperty("processed_date")
        private String processedDate;

        @JsonProperty("received_feedback_payload")
        private ResponseMetadata responseMetadata;

        public void setId(int id) {
            this.id = id;
        }

        public void setReferralNo(String referralNo) {
            this.referralNo = referralNo;
        }

        public void setReceivedDate(String receivedDate) {
            this.receivedDate = receivedDate;
        }

        public void setProcessedDate(String processedDate) {
            this.processedDate = processedDate;
        }

        public void setReferralResponse(ResponseMetadata referralResponse) {
            this.responseMetadata = referralResponse;
        }

        public int getId() {
            return id;
        }

        public String getReferralNo() {
            return referralNo;
        }

        public String getReceivedDate() {
            return receivedDate;
        }

        public String getProcessedDate() {
            return processedDate;
        }

        public ResponseMetadata getResponseMetadata() {
            return responseMetadata;
        }

    }

    public static class ResponseMetadata {

        ObjectMapper objectMapper = new ObjectMapper();

        @JsonProperty("referralNo")
        private String referralNo;

        @JsonProperty("referralFeedbackDate")
        private String referralFeedbackDate;

        @JsonProperty("servicesProvided")
        private List<ServicesProvided> servicesProvided;

        @JsonProperty("prescriptions")
        private List<Prescriptions> prescriptions;

        @JsonProperty("outcomes")
        private List<Outcomes> outcomes;

        public void setRefferralNo(String referralNo) {
            this.referralNo = referralNo;
        }

        public void setReferralFeedbackDate(String referralFeedbackDate) {
            this.referralFeedbackDate = referralFeedbackDate;
        }

        public void setServicesProvided(List<ServicesProvided> servicesProvided) {
            this.servicesProvided = servicesProvided;
        }

        public void setPrescriptions(List<Prescriptions> prescriptions) {
            this.prescriptions = prescriptions;
        }

        public void setOutcomes(List<Outcomes> outcomes) {
            this.outcomes = outcomes;
        }

        public String getRefferralNo() {
            return referralNo;
        }

        public String getReferralFeedbackDate() {
            return referralFeedbackDate;
        }

        public List<ServicesProvided> getServicesProvided() {
            return servicesProvided;
        }

        public List<Prescriptions> getPrescriptions() {
            return prescriptions;
        }

        public List<Outcomes> getOutcomes() {
            return outcomes;
        }

        public String outcomeToJsonString(){
            try {
                // Convert Outcomes object to JSON string
                return objectMapper.writeValueAsString(getOutcomes());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "";
            }
        }

    }
    
    public static class EventMetadata {

        @JsonProperty("baseentityid")
        private String baseEntityId;

        @JsonProperty("locationid")
        private String locationId;

        @JsonProperty("providerid")
        private String providerId;

        @JsonProperty("team")
        private String team;

        @JsonProperty("teamid")
        private String teamId;

        public void setBaseEntityId(String baseEntityId) {
            this.baseEntityId = baseEntityId;
        }

        public String getBaseEntityId() {
            return baseEntityId;
        }

        public void setLocationId(String locationId) {
            this.locationId = locationId;
        }

        public String getLocationId() {
            return locationId;
        }

        public void setProvideId(String provideId) {
            this.providerId = provideId;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setTeam(String team) {
            this.team = team;
        }

        public void setTeamId(String teamId) {
            this.teamId = teamId;
        }

        public String getTeam() {
            return team;
        }

        public String getTeamId() {
            return teamId;
        }
    }

    public static class ServicesProvided {
        
        @JsonProperty("serviceCode")
        private String serviceCode;

        @JsonProperty("serviceName")
        private String serviceName;

        @JsonProperty("hfrCode")
        private String hfrCode;

        @JsonProperty("facilityName")
        private String facilityName;

        public String getFacilityName() {
            return facilityName;
        }
        
        public void setFacilityName(String facilityName) {
            this.facilityName = facilityName;
        }

        public String getHfrCode() {
            return hfrCode;
        }

        public void setHfrCode(String hfrCode) {
            this.hfrCode = hfrCode;
        }

        public String getServiceCode() {
            return serviceCode;
        }

        public void setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }

    public static class Prescriptions {
        
        @JsonProperty("prescriptionCode")
        private String prescriptionCode;

        @JsonProperty("prescriptionName")
        private String prescriptionName;

        @JsonProperty("isDispensed")
        private boolean isDispensed;

        public void setDespensed(boolean isDispensed) {
            this.isDispensed = isDispensed;
        }

        public void setPrescriptionCode(String prescriptionCode) {
            this.prescriptionCode = prescriptionCode;
        }

        public void setPrescriptionName(String prescriptionName) {
            this.prescriptionName = prescriptionName;
        }

        public String getPrescriptionCode() {
            return prescriptionCode;
        }

        public String getPrescriptionName() {
            return prescriptionName;
        }

        public boolean isDespensed() {
            return isDispensed;
        }

    }

    public static class  Outcomes {

        @JsonProperty("pregnancyConfirmation")
        private PregnancyConfirmation pregnancyConfirmation;

        public PregnancyConfirmation getPregnancyConfirmation() {
            return pregnancyConfirmation;
        }

        public void setPregnancyConfirmation(PregnancyConfirmation pregnancyConfirmation) {
            this.pregnancyConfirmation = pregnancyConfirmation;
        }

    }

    public static class PregnancyConfirmation {
        @JsonProperty("status")
        private String status;

        @JsonProperty("hasDangerSigns")
        private boolean hasDangerSign;

        @JsonProperty("lnmp")
        private String lnmp;

        @JsonProperty("edd")
        private String edd;

        @JsonProperty("para")
        private Integer para;

        @JsonProperty("gravida")
        private Integer gravida;

        @JsonProperty("medicalAndSurgicalHistory")
        private List<MedicalAndSurgicalHistory> medicalAndSurgicalHistories;

        public void setEdd(String edd) {
            this.edd = edd;
        }

        public void setGravida(Integer gravida) {
            this.gravida = gravida;
        }
        
        public void setHasDangerSign(boolean hasDangerSign) {
            this.hasDangerSign = hasDangerSign;
        }

        public void setLnmp(String lnmp) {
            this.lnmp = lnmp;
        }
        
        public void setMedicalAndSurgicalHistories(List<MedicalAndSurgicalHistory> medicalAndSurgicalHistories) {
            this.medicalAndSurgicalHistories = medicalAndSurgicalHistories;
        }

        public void setPara(Integer para) {
            this.para = para;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getEdd() {
            return edd;
        }

        public Integer getGravida() {
            return gravida;
        }

        public String getLnmp() {
            return lnmp;
        }

        public List<MedicalAndSurgicalHistory> getMedicalAndSurgicalHistories() {
            return medicalAndSurgicalHistories;
        }

        public Integer getPara() {
            return para;
        }

        public String getStatus() {
            return status;
        }

        public static class MedicalAndSurgicalHistory {
            
            @JsonProperty("code")
            private String code;

            @JsonProperty("name")
            private String name;

            public void setCode(String code) {
                this.code = code;
            }

            public String getCode() {
                return code;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }

    }
}
