package com.extremum.sharedmodels.basic;

import com.extremum.common.stucts.Multilingual;
import com.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@DocumentationName("Multilingual")
public class MultilingualObject {
    private Type type;
    private String text;
    private Map<Multilingual, String> map;

    private MultilingualObject(Type type, String text, Map<Multilingual, String> map) {
        this.type = type;
        this.text = text;
        this.map = map;
    }

    public MultilingualObject() {
        this(Type.UNKNOWN, "", new HashMap<>());
    }

    public MultilingualObject(String text) {
        type = Type.TEXT;
        this.text = text;
    }

    public MultilingualObject(Map<Multilingual, String> map) {
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
