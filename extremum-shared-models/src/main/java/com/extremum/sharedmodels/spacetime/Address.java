package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.MultilingualObject;

import javax.validation.constraints.NotNull;

@DocumentationName("Address")
public class Address {
    @NotNull
    public MultilingualObject string;

    @NotNull
    public Locator locality;

    public enum FIELDS {
        string, locality
    }
}
