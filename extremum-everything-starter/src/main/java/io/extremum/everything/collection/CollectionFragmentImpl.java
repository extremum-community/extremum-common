package io.extremum.everything.collection;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
final class CollectionFragmentImpl<T> implements CollectionFragment<T> {
    private final Collection<T> elements;
    private final Long total;

    CollectionFragmentImpl(Collection<T> elements, long total) {
        this(elements, (Long) total);
    }

    CollectionFragmentImpl(Collection<T> elements, Long total) {
        Objects.requireNonNull(elements, "Elements is null");
        this.elements = elements;
        this.total = total;
    }

    @Override
    public Collection<T> elements() {
        return elements;
    }

    @Override
    public OptionalLong total() {
        if (total == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(total);
    }

    @Override
    public <U> CollectionFragment<U> map(Function<? super T, ? extends U> mapper) {
        List<U> mappedElements = elements.stream()
                .map(mapper)
                .collect(Collectors.toList());
        return new CollectionFragmentImpl<>(mappedElements, total);
    }
}
