package com.extremum.common.collection.conversion;

import com.extremum.common.dto.AbstractResponseDto;
import com.extremum.common.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author rpuch
 */
@RunWith(MockitoJUnitRunner.class)
public class ResponseCollectionsMakeupAdviceTest {
    @InjectMocks
    private ResponseCollectionsMakeupAdvice advice;

    @Mock
    private CollectionMakeup makeup;

    @Mock
    private ServerHttpRequest request;
    @Mock
    private ServerHttpResponse response;

    @Test
    public void givenReturnedValueIsResponse_whenSupportsIsCalled_thenShouldReturnTrue() {
        boolean supports = advice.supports(returnType(ReturnsResponse.class), null);
        assertThat(supports, is(true));
    }

    @Test
    public void givenReturnedValueIsNotResponse_whenSupportsIsCalled_thenShouldReturnFalse() {
        boolean supports = advice.supports(returnType(ReturnsString.class), null);
        assertThat(supports, is(false));
    }

    @Test
    public void givenReturnedValueIsResponseWithDto_whenBeforeBedyWriteIsCalled_thenMakeupIsApplied() {
        TestResponseDto dto = new TestResponseDto();
        Response responseValue = Response.ok(dto);

        advice.beforeBodyWrite(responseValue, returnType(ReturnsResponse.class), MediaType.APPLICATION_JSON,
                null, request, response);

        verify(makeup).applyCollectionMakeup(dto);
    }

    @Test
    public void givenReturnedValueIsResponseWithNonDto_whenBeforeBedyWriteIsCalled_thenMakeupIsNotApplied() {
        Response responseValue = Response.ok("test");

        advice.beforeBodyWrite(responseValue, returnType(ReturnsResponse.class), MediaType.APPLICATION_JSON,
                null, request, response);

        verify(makeup, never()).applyCollectionMakeup(any());
    }

    @Test
    public void givenReturnedValueIsNull_whenBeforeBedyWriteIsCalled_thenMakeupIsNotApplied() {
        Response responseValue = null;

        advice.beforeBodyWrite(responseValue, returnType(ReturnsResponse.class), MediaType.APPLICATION_JSON,
                null, request, response);

        verify(makeup, never()).applyCollectionMakeup(any());
    }

    @Test
    public void givenReturnedValueIsResponseWithNullResult_whenBeforeBedyWriteIsCalled_thenMakeupIsNotApplied() {
        Response responseValue = Response.ok(null);

        advice.beforeBodyWrite(responseValue, returnType(ReturnsResponse.class), MediaType.APPLICATION_JSON,
                null, request, response);

        verify(makeup, never()).applyCollectionMakeup(any());
    }

    private MethodParameter returnType(Class<?> handlerClass) {
        try {
            return new MethodParameter(handlerClass.getMethod("handle"), -1);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ReturnsResponse {
        public Response handle() {
            return null;
        }
    }

    private static class ReturnsString {
        public String handle() {
            return "test";
        }
    }

    private static class TestResponseDto extends AbstractResponseDto {

    }
}