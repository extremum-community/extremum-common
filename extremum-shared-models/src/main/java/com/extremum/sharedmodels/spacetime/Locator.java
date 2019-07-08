package com.extremum.sharedmodels.spacetime;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.StringOrMultilingual;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@DocumentationName("Locator")
public class Locator {
    private String code;
    private Type type;
    private StringOrMultilingual name;
    
    public enum Type {
        @JsonAlias("zip")
        POSTCODE,
        GALAXY,
        @JsonAlias("pleiad")
        CONSTELLATION,
        @JsonAlias({"star", "comet", "asteroid"})
        PLANET,
        CONTINENT,
        @JsonAlias({"federation", "commonwealth", "empire"})
        UNION,
        @JsonAlias({"republic", "kingdom"})
        COUNTRY,
        STATE,
        @JsonAlias({"area", "district", "country", "province", "canton", "okrug", "oblast", "estate", "parish"})
        REGION,
        @JsonAlias("town")
        CITY,
        @JsonAlias({"road", "drive", "lane", "avenue"})
        STREET,
        @JsonAlias({"building", "terminal"})
        HOUSE,
        @JsonAlias("level")
        FLOOR,
        @JsonAlias({"perron", "pier"})
        PLATFORM,
        @JsonAlias({"sector", "lot"})
        SECTION,
        @JsonProperty("apartment")
        @JsonAlias({"hall","office","room","compartment","cabinet","booth"})
        APARTMENT,
        @JsonAlias({"gate","door","porch"})
        ENTRANCE,
        @JsonAlias({"train","voyage"})
        FLIGHT,
        @JsonAlias({"carriage","bus"})
        COACH,
        @JsonAlias({"table","balcony"})
        ROW,
        @JsonAlias({"seat","stand","berth","bench","shelf","box","cell"})
        PLACE
    }
}
