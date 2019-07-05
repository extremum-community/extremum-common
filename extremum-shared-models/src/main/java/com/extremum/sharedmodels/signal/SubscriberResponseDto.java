package com.extremum.sharedmodels.signal;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.fundamental.CommonResponseDto;

import java.util.Set;

@DocumentationName("Subscriber")
public class SubscriberResponseDto extends CommonResponseDto {
    public static final String MODEL_NAME = "Subscriber";

    public String name;
    public Set<String> signals;
    public String url;

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
