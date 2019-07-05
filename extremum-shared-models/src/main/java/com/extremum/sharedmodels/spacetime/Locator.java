package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.MultilingualObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
@DocumentationName("Locator")
public class Locator {
    private String code;
    private Type type;
    private MultilingualObject name;
    
    public enum Type {
        POSTCODE("postcode"),
        GALAXY("galaxy"),
        CONSTELLATION("constellation"),
        PLANET("planet"),
        CONTINENT("continent"),
        UNION("union"),
        COUNTRY("country"),
        STATE("state"),
        REGION("region"),
        CITY("city"),
        STREET("street"),
        HOUSE("house"),
        FLOOR("floor"),
        PLATFORM("platform"),
        SECTION("section"),
        APARTMENT("apartment"),
        ENTRANCE("entrance"),
        FLIGHT("flight"),
        COACH("coach"),
        ROW("row"),
        PLACE("place");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Type fromString(String value) {
            if (value != null) {
                for (Type type : Type.values()) {
                    if (value.equalsIgnoreCase(type.getValue())) {
                        return type;
                    }
                }
            }

            return null;
        }
    }
}
