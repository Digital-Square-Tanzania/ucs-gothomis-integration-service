package com.abt.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityLinkageRequest {
    @JsonProperty("identifiers")
    private Identifiers identifiers;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("birthDate")
    private String birthDate;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("maritalStatus")
    private String maritalStatus;

    @JsonProperty("chwUsername")
    private String chwUsername;

    @JsonProperty("reason")
    private String reason;

    public Identifiers getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Identifiers identifiers) {
        this.identifiers = identifiers;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getChwUsername() {
        return chwUsername;
    }

    public void setChwUsername(String chwUsername) {
        this.chwUsername = chwUsername;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Identifiers {
        @JsonProperty("typeOfIdentifier")
        private String typeOfIdentifier;

        @JsonProperty("value")
        private String value;

        public String getTypeOfIdentifier() {
            return typeOfIdentifier;
        }

        public void setTypeOfIdentifier(String typeOfIdentifier) {
            this.typeOfIdentifier = typeOfIdentifier;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
