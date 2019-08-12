package io.extremum.sharedmodels.basic;

import io.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rpuch
 */
@Getter
@ToString
@DocumentationName("Multilingual")
public class Multilingual {
    private final Map<MultilingualLanguage, String> map;

    public Multilingual(Map<MultilingualLanguage, String> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }
}
