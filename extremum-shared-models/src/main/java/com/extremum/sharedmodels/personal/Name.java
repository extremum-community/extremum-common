package com.extremum.sharedmodels.personal;

import com.extremum.sharedmodels.basic.StringOrMultilingual;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Name {
    private StringOrMultilingual full;
    private StringOrMultilingual preferred;
    private StringOrMultilingual first;
    private StringOrMultilingual middle;
    private StringOrMultilingual last;
    private StringOrMultilingual maiden;
    private StringOrMultilingual patronymic;
    private StringOrMultilingual matronymic;
}
