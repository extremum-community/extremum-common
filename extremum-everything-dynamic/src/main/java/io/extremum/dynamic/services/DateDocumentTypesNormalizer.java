package io.extremum.dynamic.services;

import org.bson.Document;

import java.util.Collection;

public interface DateDocumentTypesNormalizer {
    Document normalize(Document doc, Collection<String> datePaths);
}
