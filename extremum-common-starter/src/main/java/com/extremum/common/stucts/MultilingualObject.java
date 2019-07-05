package com.extremum.common.stucts;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
public class MultilingualObject implements Serializable {
    public Type type;
    public String text;
    public Map<MultilingualLanguage, String> map;

    public MultilingualObject(Type type, String text, Map<MultilingualLanguage, String> map) {
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

    public MultilingualObject(Map<MultilingualLanguage, String> map) {
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
