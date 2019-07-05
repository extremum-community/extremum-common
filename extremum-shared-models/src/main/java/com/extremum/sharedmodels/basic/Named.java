package com.extremum.sharedmodels.basic;

import com.extremum.sharedmodels.annotation.DocumentationName;

/**
 * @author rpuch
 */
@DocumentationName("Named")
public interface Named {
    /**
     * The user-friendly and URL-valid name of the data element.
     */
    String getSlug();
    void setSlug(String slug);

    /**
     * The display name of the object.
     */
    StringOrMultilingual getName();
    void setName(StringOrMultilingual name);

    /**
     * A brief description of the object. Markdown is very welcome for formatting 🤗
     */
    StringOrMultilingual getDescription();
    void setDescription(StringOrMultilingual description);
}
