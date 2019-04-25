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
        this.count = list.size();
        this.top = list;
    }

    public void setDescriptor(CollectionDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
