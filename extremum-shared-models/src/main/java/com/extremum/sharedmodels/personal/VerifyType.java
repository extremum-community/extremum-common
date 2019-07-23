package com.extremum.sharedmodels.personal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum VerifyType {
    @JsonProperty("email")
    EMAIL(0),
    @JsonProperty("sms")
    SMS(0),
    @JsonProperty("email.verify")
    EMAIL_VERIFY(1),
    @JsonProperty("sms.verify")
    SMS_VERIFY(1),
    @JsonProperty("username")
    USERNAME(-1),
    @JsonProperty("password")
    PASSWORD(-1);

    private final int order;

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
