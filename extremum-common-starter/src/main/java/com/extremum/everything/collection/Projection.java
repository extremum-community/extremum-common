package com.extremum.everything.collection;

import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.DateUtils;
import lombok.Getter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author rpuch
 */
@Getter
@ToString
public class Projection {
    private final Integer offset;
    private final Integer limit;
    private final ZonedDateTime since;
    private final ZonedDateTime until;

    @ConstructorProperties({"offset", "limit", "since", "until"})
    public Projection(Integer offset, Integer limit,
            @DateTimeFormat(pattern = DateUtils.FORMAT) ZonedDateTime since,
            @DateTimeFormat(pattern = DateUtils.FORMAT) ZonedDateTime until) {
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
