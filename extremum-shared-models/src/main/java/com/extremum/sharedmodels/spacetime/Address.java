package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.MultilingualObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@DocumentationName("Address")
public class Address {
    @NotNull
    private MultilingualObject string;

    @NotNull
    private Locator locality;

    public enum FIELDS {
        string, locality
    }
}
