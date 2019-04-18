package com.extremum.common.stucts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Media implements Serializable {
    public String url;
    public Type type;
    public Integer width;
    public Integer height;
    public Integer depth;
    public IntegerOrString duration;
    public List<Media> thumbnails;

    public enum FIELDS {
        url, type, width, height, depth, duration, previews
    }

    public enum Type {
        TEXT("text"),
        IMAGE("image"),
        AUDIO("audio"),
        VIDEO("video"),
        APPLICATION("application"),
        IMAGE_JPEG("image/jpeg"),
        IMAGE_GIF("image/gif"),
        IMAGE_PNG("image/png");

        private String value;

        private Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return this.value;
        }

        @JsonCreator
        public static Media.Type fromString(String value) {
            if (value != null) {
                Media.Type[] var1 = values();
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    Media.Type object = var1[var3];
                    if (value.equalsIgnoreCase(object.value)) {
                        return object;
                    }
                }
            }

            return null;
        }
    }
}
