package com.extremum.sharedmodels.fundamental;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.MultilingualObject;

/**
 * @author rpuch
 */
@DocumentationName("Resource")
public interface Resource {
    /**
     * The type of the object as a Resource.
     */
    String getType();
    void setType(String type);

    /**
     * The status of the object as a Resource.
     * Allowed values: draft, active, hidden
     */
    String getStatus();
    void setStatus(String status);

    /**
     * The user-friendly and URL-valid name of the object as a Resource.
     */
    String getSlug();
    void setSlug(String slug);

    /**
     * The URI of the object as a Resource.
     *
     * format: uri
     */
    String getUrl();
    void setUrl(String url);

    /**
     * The display name of the object as a Resource.
     */
    MultilingualObject getName();
    void setName(MultilingualObject name);

    /**
     * A brief description of the object as a Resource. Markdown is very welcome for formatting 🤗
     */
    MultilingualObject getDescription();
    void setDescription(MultilingualObject description);
}
