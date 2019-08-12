package io.extremum.watch;

import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.response.Response;
import io.extremum.common.response.ResponseStatusEnum;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.IOException;
import java.io.StringReader;

import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
public class Tests {
    public static Matcher<? super String> successfulResponse() {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                SystemJsonObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));
                Response response = parseResponse(item, mapper);
                return response.getStatus() == ResponseStatusEnum.OK && response.getCode() == 200;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Successful response with status OK and code 200");
            }
        };
    }

    private static Response parseResponse(String item, SystemJsonObjectMapper mapper) {
        try {
            return mapper.readValue(new StringReader(item), Response.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
