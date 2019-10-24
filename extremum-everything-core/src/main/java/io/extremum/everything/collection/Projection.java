package io.extremum.everything.collection;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.utils.DateUtils;
import io.extremum.sharedmodels.constants.DateConstants;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author rpuch
 */
@ToString
public class Projection {
    private final Integer offset;
    private final Integer limit;
    private final ZonedDateTime since;
    private final ZonedDateTime until;

    @ConstructorProperties({"offset", "limit", "since", "until"})
    public Projection(Integer offset, Integer limit,
                      @DateTimeFormat(pattern = DateConstants.FORMAT) ZonedDateTime since,
                      @DateTimeFormat(pattern = DateConstants.FORMAT) ZonedDateTime until) {
        this.offset = offset;
        this.limit = limit;
        this.since = since;
        this.until = until;
    }

    public static Projection empty() {
        return new Projection(null, null, null, null);
    }

    public static Projection sinceUntil(ZonedDateTime since, ZonedDateTime until) {
        return new Projection(null, null, since, until);
    }

    public static Projection offsetLimit(Integer offset, Integer limit) {
        return new Projection(offset, limit, null, null);
    }

    // Getters

    private OptionalInt ofNullable(Integer value) {
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    public OptionalInt getOffset() {
        return ofNullable(offset);
    }

    public OptionalInt getLimit() {
        return ofNullable(limit);
    }

    public Optional<ZonedDateTime> getSince() {
        return Optional.ofNullable(since);
    }

    public Optional<ZonedDateTime> getUntil() {
        return Optional.ofNullable(until);
    }

    public boolean definesFilteringOnCreationDate() {
        return since != null || until != null;
    }

    // Public methods

    public boolean accepts(PersistableCommonModel<?> model) {
        if (since != null && model.getCreated() != null) {
            if (model.getCreated().isBefore(since)) {
                return false;
            }
        }
        if (until != null && model.getCreated() != null) {
            if (model.getCreated().isAfter(until)) {
                return false;
            }
        }

        return true;
    }

    public <T> List<T> cut(List<T> list) {
        int startInclusive = offset == null ? 0 : offset;
        startInclusive = Math.max(startInclusive, 0);
        if (startInclusive > list.size()) {
            return Collections.emptyList();
        }

        int endExclusive = limit == null ? list.size() : startInclusive + limit;
        endExclusive = Math.min(endExclusive, list.size());

        return list.subList(startInclusive, endExclusive);
    }
}
