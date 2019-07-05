package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.StringOrMultilingual;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
@DocumentationName("Address")
public class Address {
    @NotNull
    private StringOrMultilingual name;

    @NotNull
    private List<Locator> locality;

    public enum FIELDS {
        string, locality
    }
}
