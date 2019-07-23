package com.extremum.sharedmodels.personal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

public enum VerifyType {
    @JsonProperty("email")
    EMAIL,
    @JsonProperty("sms")
    SMS,
    @JsonProperty("email.verify")
    EMAIL_VERIFY,
    @JsonProperty("sms.verify")
    SMS_VERIFY,
    @JsonProperty("username")
    USERNAME,
    @JsonProperty("password")
    PASSWORD;

    boolean inSameGroupWith(VerifyType otherType) {
        Set<VerifyType> thisAndThat = new HashSet<>();
        thisAndThat.add(this);
        thisAndThat.add(otherType);

        if (thisAndThat.contains(USERNAME) && thisAndThat.contains(PASSWORD)) {
            return true;
        }

        return false;
    }
}
