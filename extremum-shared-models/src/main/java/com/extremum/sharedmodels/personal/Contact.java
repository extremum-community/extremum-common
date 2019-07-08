package com.extremum.sharedmodels.personal;

import com.extremum.sharedmodels.annotation.DocumentationName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DocumentationName("Contact")
public class Contact {
    private String type;
    private String contact;
    private boolean primary;
}
