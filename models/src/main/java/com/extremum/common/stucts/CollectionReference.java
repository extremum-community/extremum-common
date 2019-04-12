package com.extremum.common.stucts;

import com.extremum.common.descriptor.Descriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vov4a on 05.04.2018.
 */
public class CollectionReference<T> {
    public Descriptor id;

    public String url;

    public Integer count = 0;

    public List<T> top = new ArrayList<>();

    public enum FIELDS {
        id, url, count, top
    }
}
