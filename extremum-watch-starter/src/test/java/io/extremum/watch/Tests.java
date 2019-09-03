package io.extremum.watch;

import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.response.Response;
import io.extremum.common.response.ResponseStatusEnum;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
public class Tests {
    public static Matcher<? super String> response(Matcher<? super Response> responseMatcher) {
        return new ResponseMatcher(responseMatcher);
    }

    public static Matcher<? super String> responseThat(Matcher<? super Response> responseMatcher) {
        return response(responseMatcher);
    }

    public static Matcher<? super String> successfulResponse() {
        return responseThat(
                allOf(
                        hasProperty("status", is(ResponseStatusEnum.OK)),
                        hasProperty("code", is(200))
                )
        );
    }

    private static Response parseResponse(String item, SystemJsonObjectMapper mapper) {
        try {
            return mapper.readValue(new StringReader(item), Response.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ResponseMatcher extends TypeSafeDiagnosingMatcher<String> {
        private final Matcher<? super Response> responseMatcher;

        private SystemJsonObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));

        ResponseMatcher(Matcher<? super Response> responseMatcher) {
            this.responseMatcher = responseMatcher;
        }

        @Override
        protected boolean matchesSafely(String item, Description mismatchDescription) {
            Response response;
            try {
                response = parseResponse(item, mapper);
            } catch (Exception e) {
                mismatchDescription.appendText(
                        "JSON parseable as JSON but could not parse because of\n" + extractStackTrace(e));
                return false;
            }
            if (!responseMatcher.matches(response)) {
                responseMatcher.describeMismatch(response, mismatchDescription);
                return false;
            }
            return true;
        }

        private String extractStackTrace(Exception e) {
            StringWriter writer = new StringWriter();
            try (PrintWriter printWriter = new PrintWriter(writer)) {
                e.printStackTrace(printWriter);
            }
            return writer.toString();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Parseable response that ").appendDescriptionOf(responseMatcher);
        }
    }
}
