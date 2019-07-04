package com.extremum.sharedmodels.content;

import com.extremum.common.stucts.Media;
import com.extremum.sharedmodels.annotation.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * The Preview of the object, which allows to give a textual and visual explanation about it without having its data fetched.
 */
public class Preview {
    /**
     * Timestamp of the Preview in ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss.SSSZ).
     */
    public String timestamp;

    /**
     * A plain text caption that represents the object.
     */

    @NotNull
    @NotEmpty
    public String caption;
    public Media icon;
    public Media splash;
}
