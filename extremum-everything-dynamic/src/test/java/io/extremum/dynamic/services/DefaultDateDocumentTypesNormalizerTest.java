package io.extremum.dynamic.services;

import com.jayway.jsonpath.JsonPath;
import io.extremum.dynamic.services.impl.DefaultDateDocumentTypesNormalizer;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultDateDocumentTypesNormalizerTest {
    @Test
    void normalizeDatesInDocument_allPathsExists() {
        Set<String> datePaths = new HashSet<>();
        String json = "{\n" +
                "  \"peoples\": [\n" +
                "    {\n" +
                "      \"name\": \"Noe\",\n" +
                "      \"birth\": \"2010-01-09T09:31:26.000251-0500\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Moll\",\n" +
                "      \"birth\": \"2011-01-09T09:31:26.000251-0500\",\n" +
                "      \"properties\": [\n" +
                "        {\"prop1\":  \"value\"},\n" +
                "        {\"date\": \"2012-01-09T09:31:26.000251-0500\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"created\": {\n" +
                "    \"date\": \"2013-01-09T09:31:26.000251-0500\"\n" +
                "  }\n" +
                "}\n";

        datePaths.add("$.peoples[0].birth");
        datePaths.add("$.peoples[1].birth");
        datePaths.add("$.peoples[1].properties[1].date");
        datePaths.add("$.created.date");

        Document doc = Document.parse(json);

        DateDocumentTypesNormalizer normalizer = new DefaultDateDocumentTypesNormalizer();
        Document normalized = normalizer.normalize(doc, datePaths);

        Function<String, ZonedDateTime> byPath = path -> getDateByPath(normalized, path);

        assertEquals(2010, byPath.apply("$.peoples[0].birth").getYear());
        assertEquals(2011, byPath.apply("$.peoples[1].birth").getYear());
        assertEquals(2012, byPath.apply("$.peoples[1].properties[1].date").getYear());
        assertEquals(2013, byPath.apply("$.created.date").getYear());
    }

    @Test
    void normalizeDatesInDocument_pathDoesntExists() {
        Document doc = Document.parse("{}");
        Set<String> datePaths = new HashSet<>();
        datePaths.add("$.a.b.c");

        DateDocumentTypesNormalizer normalizer = new DefaultDateDocumentTypesNormalizer();
        Document normalized = normalizer.normalize(doc, datePaths);

        assertEquals(doc.toJson(), normalized.toJson());
    }

    private ZonedDateTime getDateByPath(Document doc, String path) {
        Date date = JsonPath.read(doc, path);
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }
}