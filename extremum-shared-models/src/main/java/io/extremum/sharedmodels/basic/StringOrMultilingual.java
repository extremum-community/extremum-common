package io.extremum.sharedmodels.basic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class StringOrMultilingual implements Serializable {
    private Type type;
    private String text;
    private Multilingual multilingual;

    private StringOrMultilingual(Type type, String text, Map<MultilingualLanguage, String> map) {
        this.type = type;
        this.text = text;
        this.multilingual = new Multilingual(map);
    }

    public StringOrMultilingual() {
        this(Type.UNKNOWN, "", new HashMap<>());
    }

    public StringOrMultilingual(String text) {
        type = Type.TEXT;
        this.text = text;
    }

    public StringOrMultilingual(Map<MultilingualLanguage, String> map) {
        type = Type.MAP;
        this.multilingual = new Multilingual(map);
    }

    public boolean isTextOnly() {
        return type == Type.TEXT;
    }

    public boolean isMultilingual() {
        return type == Type.MAP;
    }

    public boolean isKnown() {
        return type != Type.UNKNOWN;
    }

    public enum Type {
        UNKNOWN, TEXT, MAP
    }

    public enum FIELDS {
        type, text, map
    }
}
