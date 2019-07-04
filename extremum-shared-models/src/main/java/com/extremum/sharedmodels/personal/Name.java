package com.extremum.sharedmodels.personal;

import com.extremum.common.stucts.MultilingualObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Name {
    private MultilingualObject full;
    private MultilingualObject preferred;
    private MultilingualObject first;
    private MultilingualObject middle;
    private MultilingualObject last;
    private MultilingualObject maiden;
    private MultilingualObject patronymic;
    private MultilingualObject matronymic;
}
