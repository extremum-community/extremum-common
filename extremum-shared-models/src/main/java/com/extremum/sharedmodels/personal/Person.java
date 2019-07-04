package com.extremum.sharedmodels.personal;

import com.extremum.common.stucts.IdOrObjectStruct;
import com.extremum.common.stucts.Media;
import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.spacetime.Address;
import com.extremum.sharedmodels.spacetime.Position;
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
public class Person {
    private IdOrObjectStruct<String, Name> name;
    private Gender gender;
    private int age;
    private Birth birth;
    private String nationality;
    private List<Language> languages;
    private String hometown;
    private List<Address> addresses;
    private List<Contact> contacts;
    private List<Position> positions;
    private String relationship;
    private List<Media> images;
    private List<Object> documents;
}
