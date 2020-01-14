package io.extremum.dynamic.services.impl;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.sharedmodels.constants.DateConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DefaultDateTypesNormalizer implements DateTypesNormalizer {
    @Override
    public Map<String, Object> normalize(Map<String, Object> doc, Collection<String> datePaths) {
        for (String path : datePaths) {
            try {
                Object value = JsonPath.read(doc, path);
                if (value != null) {
                    ZonedDateTime zdt = toZonedDateTime((String) value);
                    JsonPath.compile(path)
                            .set(doc, Date.from(zdt.toInstant()), Configuration.builder().build());
                }
            } catch (PathNotFoundException e) {
                log.warn("Path {} wasn't found in doc {}", path, doc);
            }
        }

        return doc;
    }

    private ZonedDateTime toZonedDateTime(String value) {
        return ZonedDateTime.parse(value, DateTimeFormatter.ofPattern(DateConstants.FORMAT));
    }
}
