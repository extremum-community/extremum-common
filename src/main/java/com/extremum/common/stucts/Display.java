package com.extremum.common.stucts;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Display implements Serializable {
    private Type type;
    public String stringValue;
    private MultilingualObject caption;
    private Media icon;
    private Media splash;

    public Display(String value) {
        type = Type.string;
        stringValue = value;
    }

    public Display(MultilingualObject caption, Media icon, Media splash) {
        type = Type.object;
        this.caption = caption;
        this.icon = icon;
        this.splash = splash;
    }


    public boolean isString() {
        return Type.string.equals(type);
    }

    public boolean isObject() {
        return Type.object.equals(type);
    }

    public enum Type {
        string, object
    }

    public enum FIELDS {
        caption, icon, splash
    }
}
