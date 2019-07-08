package com.extremum.sharedmodels.personal;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.StringOrObject;
import com.extremum.sharedmodels.content.Media;
import com.extremum.sharedmodels.dto.RequestDto;
import com.extremum.sharedmodels.spacetime.CategorizedAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DocumentationName("Person")
public class PersonRequestDto implements RequestDto {
    private StringOrObject<Name> name;
    private Gender gender;
    private int age;
    private Birth birth;
    private String nationality;
    private List<Language> languages;
    private String hometown;
    private List<CategorizedAddress> addresses;
    private List<Contact> contacts;
    private List<PersonPositionForRequestDto> positions;
    private String relationship;
    private List<Media> images;
    private List<Object> documents;
}
