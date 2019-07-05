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
    MultilingualObject getName();
    void setName(MultilingualObject name);

    /**
     * A brief description of the object. Markdown is very welcome for formatting 🤗
     */
    MultilingualObject getDescription();
    void setDescription(MultilingualObject description);
}
