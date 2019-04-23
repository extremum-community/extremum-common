package com.extremum.common.collection;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rpuch
 */
@Getter
public class CollectionReference<T> {
    private CollectionDescriptor descriptor;

    private String uri;
    private Integer count = 0;
    private List<T> top = new ArrayList<>();

    public CollectionReference(List<T> list) {
        this.count = list.size();
        this.top = list;
    }

    public void setDescriptor(CollectionDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
