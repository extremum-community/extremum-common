package com.extremum.sharedmodels.personal;

import com.extremum.sharedmodels.basic.StringOrMultilingual;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Name {
    @JsonProperty("full")
    private StringOrMultilingual full;
    @JsonProperty("preferred")
    private StringOrMultilingual preferred;
    @JsonProperty("first")
    private StringOrMultilingual first;
    @JsonProperty("middle")
    private StringOrMultilingual middle;
    @JsonProperty("last")
    private StringOrMultilingual last;
    @JsonProperty("maiden")
    private StringOrMultilingual maiden;
    @JsonProperty("patronymic")
    private StringOrMultilingual patronymic;
    @JsonProperty("matronymic")
    private StringOrMultilingual matronymic;
}
