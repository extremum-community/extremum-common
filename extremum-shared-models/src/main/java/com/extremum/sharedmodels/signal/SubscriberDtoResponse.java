package com.extremum.sharedmodels.signal;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.fundamental.CommonResponseDto;

import java.util.Set;

@DocumentationName("Subscriber")
public class SubscriberDtoResponse extends CommonResponseDto {
    public String name;
    public Set<String> signals;
    public String url;
}
