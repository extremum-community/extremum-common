package com.extremum.sharedmodels.signal;

import com.extremum.common.dto.RequestDto;
import com.extremum.sharedmodels.annotation.DocumentationName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;


@DocumentationName("Subscriber")
public class SubscriberRequestDto implements RequestDto {
    @NotNull @NotEmpty
    public String name;
    @NotNull @Size(min = 1)
    public Set<String> signals;
    @NotNull @NotEmpty
    public String url;
}
