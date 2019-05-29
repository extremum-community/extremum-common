package com.extremum.common.stucts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Display implements Serializable {
    @JsonProperty("type")
    private Type type;

    @JsonProperty("stringValue")
    private String stringValue;

    @JsonProperty("caption")
    private MultilingualObject caption;

    @JsonProperty("icon")
    private Media icon;

    @JsonProperty("splash")
    private Media splash;

    public Display(String value) {
        type = Type.STRING;
        stringValue = value;
    }

    public Display(MultilingualObject caption, Media icon, Media splash) {
        type = Type.OBJECT;
        this.caption = caption;
        this.icon = icon;
        this.splash = splash;
    }

    public boolean isString() {
        return Type.STRING.equals(type);
    }

    public boolean isObject() {
        return Type.OBJECT.equals(type);
    }

    public enum Type {
        STRING, OBJECT
    }

    public enum FIELDS {
        caption, icon, splash
    }
}
