package com.extremum.sharedmodels.fundamental;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.content.Preview;
import lombok.Data;

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
@Data
@DocumentationName("Descriptor")
public class DescriptorResponseDto<T> {
    /**
     * The ID of the object referenced and described by the ViewDescriptor.
     */
    private Descriptor id;

    /**
     * The URL of the described object, which should be used for fetching its contents.
     */
    private String url;

    /**
     * Some descriptive information that represents the object referenced and described by the ViewDescriptor.
     * May be a string or {@link Preview} object
     */
    private Object display;

    /**
     * Some or all properties of the described object.
     */
    private T properties;
}
