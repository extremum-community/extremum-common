package com.extremum.common.collection;

import lombok.Getter;

import java.util.List;

/**
 * @author rpuch
 */
@Getter
public class CollectionReference<T> {
    private CollectionDescriptor descriptor;

    private String uri;
    private Integer count;
    private List<T> top;

    public CollectionReference(List<T> list) {
        this(list, list.size());
    }

    public CollectionReference(List<T> top, int count) {
        this.count = count;
        this.top = top;
    }

    public void setDescriptor(CollectionDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
