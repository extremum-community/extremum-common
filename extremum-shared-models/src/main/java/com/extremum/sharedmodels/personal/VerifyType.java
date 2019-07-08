package com.extremum.sharedmodels.personal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    SMS_VERIFY(1);

    private final int order;
}
