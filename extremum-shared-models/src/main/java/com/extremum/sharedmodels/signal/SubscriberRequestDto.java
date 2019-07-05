package com.extremum.sharedmodels.signal;

import com.extremum.common.dto.RequestDto;
import com.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;


@Getter
@Setter
@ToString
@DocumentationName("Subscriber")
public class SubscriberRequestDto implements RequestDto {
    @NotNull @NotEmpty
    private String name;
    @NotNull @Size(min = 1)
    private Set<String> signals;
    @NotNull @NotEmpty
    private String url;
}
