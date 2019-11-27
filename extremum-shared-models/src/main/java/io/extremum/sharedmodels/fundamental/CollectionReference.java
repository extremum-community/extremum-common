package io.extremum.sharedmodels.fundamental;

import io.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

/**
 * @author rpuch
 */
@Getter
@DocumentationName("Collection")
public class CollectionReference<T> {
    private String id;

    private String url;
    private Long count;
    private List<T> top;

    public static <T> CollectionReference<T> forUnknownTotalSize(List<T> top) {
        return new CollectionReference<>(top, null);
    }

    public static <T> CollectionReference<T> uninitialized() {
        return new CollectionReference<>(null, null);
    }

    public CollectionReference() {
        this(emptyList());
    }

    public CollectionReference(List<T> list) {
        this(list, list.size());
    }

    public CollectionReference(List<T> top, long total) {
        this(Objects.requireNonNull(top, "top cannot be null"), (Long) total);
    }

    private CollectionReference(List<T> top, Long total) {
        this.count = total;
        this.top = top;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTop(List<T> top) {
        Objects.requireNonNull(top, "top cannot be null");
        this.top = top;
    }
}
