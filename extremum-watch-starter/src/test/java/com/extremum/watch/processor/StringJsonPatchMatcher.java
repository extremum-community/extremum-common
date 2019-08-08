package com.extremum.watch.processor;

import com.extremum.common.mapper.MapperDependencies;
import com.extremum.common.mapper.SystemJsonObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.IOException;

import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
class StringJsonPatchMatcher extends TypeSafeDiagnosingMatcher<String> {
    private final Matcher<JsonPatch> patchMatcher;

    private final ObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));

    StringJsonPatchMatcher(Matcher<JsonPatch> patchMatcher) {
        this.patchMatcher = patchMatcher;
    }

    @Override
    protected boolean matchesSafely(String item, Description mismatchDescription) {
        JsonPatch jsonPatch;
        try {
            jsonPatch = mapper.readerFor(JsonPatch.class).readValue(item);
            if (!patchMatcher.matches(jsonPatch)) {
                mismatchDescription.appendText(" a JSON patch ");
                patchMatcher.describeMismatch(jsonPatch, mismatchDescription);
                return false;
            }
            return true;
        } catch (IOException e) {
            mismatchDescription.appendText("a JSON patch ");
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("JSON patch that ").appendDescriptionOf(patchMatcher);
    }
}
