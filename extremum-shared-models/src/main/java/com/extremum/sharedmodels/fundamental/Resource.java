package com.extremum.sharedmodels.fundamental;

import com.extremum.sharedmodels.annotation.DocumentationName;
import com.extremum.sharedmodels.basic.StringOrMultilingual;

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
    StringOrMultilingual getName();
    void setName(StringOrMultilingual name);

    /**
     * A brief description of the object as a Resource. Markdown is very welcome for formatting 🤗
     */
    StringOrMultilingual getDescription();
    void setDescription(StringOrMultilingual description);
}