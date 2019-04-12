package com.extremum.common.stucts;

import com.extremum.common.models.AbstractCommonModel;

/**
 * Common model - Resource
 */
public abstract class Resource extends AbstractCommonModel {
    /**
     * The type of the object as a Resource
     */
    public String type;

    /**
     * The status of the object as a Resource
     * Allowed Values: draft, active, hidden
     */
    public Status status;

    /**
     * The user-friendly and URL-valid name of the object as a Resource. Learn more about the slug concept
     */
    public String slug;

    /**
     * The URI of the object as a Resource
     */
    public String uri;

    /**
     * The title of the object as a Resource (or its display name)
     * May be a {@link String} (The value of the attribute given in current locale determined by context of the API request)
     * or {@link Multilingual} object (Multilingual set of strings. For specifying locales please use IETF language
     * tags in the format defined by RFC 5646 (language-TERRITORY).)
     */
    public Object name;

    /**
     * A brief description of the object as a Resource. Markdown is very welcome for formatting
     * May be a string (The value of the attribute given in current locale determined by context of the API request)
     * or {@link Multilingual} object (Multilingual set of strings. For specifying locales please use IETF language
     * tags in the format defined by RFC 5646 (language-TERRITORY))
     */
    public Object description;

    /**
     * Status of the Resource
     */
    public enum Status {
        DRAFT("DRAFT"),
        ACTIVE("ACTIVE"),
        HIDDE("HIDDEN");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status fromString(String value) {
            if (value != null) {
                for (Status status : Status.values()) {
                    if (value.equalsIgnoreCase(status.getValue())) {
                        return status;
                    }
                }
            }

            return null;
        }
    }

    public enum FIELDS {
        type, status, slug, uri, name, description
    }
}
