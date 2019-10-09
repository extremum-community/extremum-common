package io.extremum.everything.aop;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.exceptions.RequestDtoValidationException;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rpuch
 */
class DefaultEverythingEverythingExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                 .standaloneSetup(new TestController())
                 .setControllerAdvice(new DefaultEverythingEverythingExceptionHandler())
                 .build();
    }

    @Test
    void whenNothingIsThrown_thenOriginalResponseShouldBeReturned() throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/ok");

        assertThat(root.getString("status"), is("OK"));
        assertThat(root.getInt("code"), is(200));
        assertThat(root.getString("result"), is("Success!"));
    }

    @NotNull
    private JSONObject parseResponse(MvcResult result) throws UnsupportedEncodingException, JSONException {
        String content = result.getResponse().getContentAsString();
        return new JSONObject(content);
    }

    @Test
    void whenModelNotFoundExceptionIsThrown_thenProper404ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/model-not-found");

        assertThat(root.getString("status"), is("FAIL"));
        assertThat(root.getInt("code"), is(404));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @NotNull
    private JSONObject getSuccessfullyAndParseResponse(String uri) throws Exception {
        MvcResult result = mockMvc.perform(get(uri))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        return parseResponse(result);
    }

    @Test
    void whenEvrEvrExceptionIsThrown_thenFail200ResponseShouldBeReturned()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/evr-evr-exception");

        assertThat(root.getString("status"), is("FAIL"));
        assertThat(root.getInt("code"), is(500));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenRequestDtoValidationExceptionIsThrown_thenFail200ResponseShouldBeReturned()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/validation-failure");

        assertThat(root.getString("status"), is("FAIL"));
        assertThat(root.getInt("code"), is(400));
        assertThat(root.getString("result"), is("Unable to complete 'everything-everything' operation"));
    }

    @Test
    void whenDescriptorNotFoundExceptionIsThrown_thenProper404ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/descriptor-not-found");

        assertThat(root.getString("status"), is("FAIL"));
        assertThat(root.getInt("code"), is(404));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenExtremumAccessDeniedExceptionIsThrown_thenProper403ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/extremum-access-denied-exception");

        assertThat(root.getString("status"), is("FAIL"));
        assertThat(root.getInt("code"), is(403));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @RestController
    @EverythingExceptionHandlerTarget
    private static class TestController {
        @RequestMapping("/ok")
        Response ok() {
            return Response.ok("Success!");
        }

        @RequestMapping("/model-not-found")
        Response modelNotFound() {
            throw new ModelNotFoundException("Did not find the model!");
        }

        @RequestMapping("/evr-evr-exception")
        Response evrEvrException() {
            throw new EverythingEverythingException("Everything-everything is lost!");
        }

        @RequestMapping("/validation-failure")
        Response validationFailure() {
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("someProperty");

            @SuppressWarnings("unchecked")
            ConstraintViolation<RequestDto> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Some failure");
            when(violation.getPropertyPath()).thenReturn(path);

            throw new RequestDtoValidationException(new TestRequestDto(), Collections.singleton(violation));
        }

        @RequestMapping("/descriptor-not-found")
        Response descriptorNotFound() {
            throw new DescriptorNotFoundException("Did not find anything");
        }

        @RequestMapping("/extremum-access-denied-exception")
        Response everythingAccessDeniedException() {
            throw new ExtremumAccessDeniedException("Access denied");
        }
    }

    private static class TestRequestDto implements RequestDto {

    }
}