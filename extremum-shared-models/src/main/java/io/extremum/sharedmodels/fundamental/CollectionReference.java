package io.extremum.sharedmodels.fundamental;

import io.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * @author rpuch
 */
@Getter
@DocumentationName("Collection")
public class CollectionReference<T> {
    private String id;

    private String url;
    private final Long count;
    private final List<T> top;

    public CollectionReference() {
        this(Collections.emptyList());
    }

    public CollectionReference(List<T> list) {
        this(list, list.size());
    }

    public CollectionReference(List<T> top, long total) {
        this.count = total;
        this.top = top;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
