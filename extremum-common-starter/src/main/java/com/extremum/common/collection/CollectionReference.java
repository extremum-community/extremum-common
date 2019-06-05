package com.extremum.common.collection;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * @author rpuch
 */
@Getter
public class CollectionReference<T> {
    private CollectionDescriptor id;

    private String url;
    private Long count;
    private List<T> top;

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

    public void setId(CollectionDescriptor id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
