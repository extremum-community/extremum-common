package io.extremum.sharedmodels.descriptor;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author rpuch
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class FreeCoordinates implements Serializable {
    private String name;
    private String parametersString;

    public FreeCoordinates(String name) {
        this(name, null);
    }

    public FreeCoordinates(String name, String parametersString) {
        Objects.requireNonNull(name, "name cannot be null");
        if (name.contains("/")) {
            throw new IllegalArgumentException("name cannot contain /");
        }

        this.name = name;
        this.parametersString = parametersString;
    }

    public String toCoordinatesString() {
        return "FREE/" + name + (parametersString == null ? "" : "/" + parametersString);
    }
}
