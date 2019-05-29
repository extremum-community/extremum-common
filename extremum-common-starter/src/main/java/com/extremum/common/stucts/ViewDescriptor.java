package com.extremum.common.stucts;

import com.extremum.common.descriptor.Descriptor;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * An object descriptior represents some Object referenced by an attribute in another Object. The descriptor has the
 * id and url that allow to fetch the referenced object, plus it also contains display attributes that allow to build
 * an object preview without having its data fetched.
 *
 * The properties section allows to give a partial extract of the referenced data object.
 *
 * The ViewDescriptor may also refer to an Object, which doesn't exist, so it doesn't have id and url. In that case the
 * data given in properties is used to create a new object.
 */
public class ViewDescriptor<T> {
    /**
     * The ID of the object referenced and described by the ViewDescriptor.
     */
    public Descriptor id;

    /**
     * The URL of the described object, which should be used for fetching its contents.
     */
    public String url;

    /**
     * Some descriptive information that represents the object referenced and described by the ViewDescriptor.
     * May be a string or {@link Preview} object
     */
    @JsonRawValue
    public Display display;

    /**
     * Some or all properties of the described object.
     */
    public T properties;

    public enum FIELDS {
        id, url, display, properties
    }
}
