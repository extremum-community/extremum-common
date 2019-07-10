package com.extremum.sharedmodels.basic;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class StringOrMultilingual implements Serializable {
    private Type type;
    private String text;
    private Map<MultilingualLanguage, String> map;

    private StringOrMultilingual(Type type, String text, Map<MultilingualLanguage, String> map) {
        this.type = type;
        this.text = text;
        this.map = map;
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
        this.map = map;
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
