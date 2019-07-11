package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.basic.Multilingual;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ComplexAddress {
    private Type type;
    private String string;
    private Multilingual multilingual;
    private Address address;

    public ComplexAddress() {
        type = Type.unknown;
    }

    public enum Type {
        unknown, string, multilingual, addressObject
    }
}
