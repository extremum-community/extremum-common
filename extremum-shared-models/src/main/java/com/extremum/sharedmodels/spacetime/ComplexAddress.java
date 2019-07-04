package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.basic.MultilingualObject;

public class ComplexAddress {
    public Type type;
    public String string;
    public MultilingualObject multilingual;
    public Address address;

    public ComplexAddress() {
        type = Type.unknown;
    }

    public enum Type {
        unknown, string, multilingual, addressObject
    }
}
