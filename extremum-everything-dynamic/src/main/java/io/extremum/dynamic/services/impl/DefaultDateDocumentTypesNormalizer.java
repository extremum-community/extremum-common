package io.extremum.dynamic.services.impl;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.extremum.dynamic.services.DateDocumentTypesNormalizer;
import io.extremum.sharedmodels.constants.DateConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class DefaultDateDocumentTypesNormalizer implements DateDocumentTypesNormalizer {
    @Override
    public Document normalize(Document doc, Collection<String> datePaths) {
        for (String path : datePaths) {
            try {
                Object value = JsonPath.read(doc, path);
                if (value != null) {
                    ZonedDateTime zdt = toZonedDateTime((String) value);
                    JsonPath.compile(path)
                            .set(doc, Date.from(zdt.toInstant()), Configuration.builder().build());
                }
            } catch (PathNotFoundException e) {
                log.warn("Path {} wasn't found in doc {}", path, doc.toJson());
            }
        }

        return doc;
    }

    private ZonedDateTime toZonedDateTime(String value) {
        return ZonedDateTime.parse(value, DateTimeFormatter.ofPattern(DateConstants.FORMAT));
    }
}
