package io.extremum.common.descriptor.resolve;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.test.aop.AspectWrapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactiveDescriptorResolvingAspectTest {
    @InjectMocks
    private ReactiveDescriptorResolvingAspect aspect;

    @Mock
    private ResponseDtoDescriptorResolver resolver;

    private final ResponseDto responseDto = mock(ResponseDto.class);
    private final Response response = Response.ok(responseDto);

    private TestController controllerProxy;

    @BeforeEach
    void prepareProxy() {
        controllerProxy = AspectWrapping.wrapInAspect(new TestController(), aspect);
    }

    @BeforeEach
    void configureMakeupToReturnEmptyMono() {
        lenient().when(resolver.resolveExternalIdsIn(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDto_whenCallingTheMethod_thenResolvingShouldBeApplied() {
        Response result = controllerProxy.returnsMonoWithResponseWithResponseDto().block();

        assertThat(result, is(sameInstance(response)));
        verifyThatExternalIdResolvingWasApplied();

    }

    private void verifyThatExternalIdResolvingWasApplied() {
        //noinspection UnassignedFluxMonoInstance
        verify(resolver).resolveExternalIdsIn(responseDto);
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDtoArray_whenCallingTheMethod_thenResolvingShouldBeApplied() {
        controllerProxy.returnsMonoWithResponseWithResponseDtoArray().block();

        verifyThatExternalIdResolvingWasApplied();
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithResponseDtoList_whenCallingTheMethod_thenResolvingShouldBeApplied() {
        controllerProxy.returnsMonoWithResponseWithResponseDtoList().block();

        verifyThatExternalIdResolvingWasApplied();
    }

    @Test
    void givenMethodReturnTypeIsMonoWithResponseAndMethodReturnsEmpty_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        Response result = controllerProxy.returnsEmptyResponseMono().block();

        assertThat(result, is(nullValue()));
        verifyThatExternalIdResolvingWasNotApplied();

    }

    private void verifyThatExternalIdResolvingWasNotApplied() {
        //noinspection UnassignedFluxMonoInstance
        verify(resolver, never()).resolveExternalIdsIn(responseDto);
    }

    @Test
    void givenMethodReturnTypeIsMonoWithString_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        String result = controllerProxy.returnsMonoWithString().block();

        assertThat(result, is("test"));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenMethodReturnsMonoWithResponseWithString_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        Response result = controllerProxy.returnsMonoWithResponseWithString().block();

        assertThat(result, is(notNullValue()));
        assertThat(result.getResult(), is("test"));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsMonoWithResponseAndItReturnsNull_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        Mono<Response> result = controllerProxy.returnsNullMono();

        assertThat(result, is(nullValue()));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponse_whenCallingTheMethod_thenResolvingShouldBeApplied() {
        Response result = controllerProxy.returnsFluxWithResponse().blockLast();

        assertThat(result, is(sameInstance(response)));
        verifyThatExternalIdResolvingWasApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponseAndMethodReturnsEmpty_whenCallingTheMethod_thenResolvingShouldBeApplied() {
        Response result = controllerProxy.returnsEmptyResponseFlux().blockLast();

        assertThat(result, is(nullValue()));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithString_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        String result = controllerProxy.returnsFluxWithString().blockLast();

        assertThat(result, is("test"));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenMethodReturnsFluxWithResponseWithString_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        Response result = controllerProxy.returnsFluxWithResponseWithString().blockLast();

        assertThat(result, is(notNullValue()));
        assertThat(result.getResult(), is("test"));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenMethodReturnTypeIsFluxWithResponseAndItReturnsNull_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        Flux<Response> result = controllerProxy.returnsNullFlux();

        assertThat(result, is(nullValue()));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenMethodReturnsFluxOfServerSentEventsWithResponseDto_whenCallingTheMethod_thenResolvingShouldBeApplied() {
        ServerSentEvent<ResponseDto> result = controllerProxy.returnsFluxOfServerSentEventsWithResponseDto()
                .blockLast();
        assertThat(result, is(notNullValue()));

        assertThat(result.data(), is(sameInstance(responseDto)));
        verifyThatExternalIdResolvingWasApplied();
    }

    @Test
    void givenMethodReturnsNonPublisher_whenCallingTheMethod_thenResolvingShouldNotBeApplied() {
        Response result = controllerProxy.returnsNonPublisher();

        assertThat(result, is(sameInstance(response)));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenTargetIsNotAController_whenInvokingAMonoMethodReturningAResponse_thenResolvingShouldNotBeApplied() {
        NotAnnotatedAsController notController = AspectWrapping.wrapInAspect(new NotAnnotatedAsController(), aspect);

        Response result = notController.returnsMonoWithResponse().block();

        assertThat(result, is(sameInstance(response)));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Test
    void givenTargetIsNotAController_whenInvokingAFluxMethodReturningAResponse_thenResolvingShouldNotBeApplied() {
        NotAnnotatedAsController notController = AspectWrapping.wrapInAspect(new NotAnnotatedAsController(), aspect);

        Response result = notController.returnsFluxWithResponse().blockLast();

        assertThat(result, is(sameInstance(response)));
        verifyThatExternalIdResolvingWasNotApplied();
    }

    @Controller
    private class TestController {
        Mono<Response> returnsMonoWithResponseWithResponseDto() {
            return Mono.just(response);
        }

        Mono<Response> returnsMonoWithResponseWithResponseDtoArray() {
            return Mono.just(Response.ok(new ResponseDto[]{responseDto}));
        }

        Mono<Response> returnsMonoWithResponseWithResponseDtoList() {
            return Mono.just(Response.ok(singletonList(responseDto)));
        }

        Mono<Response> returnsEmptyResponseMono() {
            return Mono.empty();
        }

        Mono<String> returnsMonoWithString() {
            return Mono.just("test");
        }

        Mono<Response> returnsMonoWithResponseWithString() {
            return Mono.just(Response.ok("test"));
        }

        Mono<Response> returnsNullMono() {
            return null;
        }

        Flux<Response> returnsFluxWithResponse() {
            return Flux.just(response);
        }

        Flux<Response> returnsEmptyResponseFlux() {
            return Flux.empty();
        }

        Flux<String> returnsFluxWithString() {
            return Flux.just("test");
        }

        Flux<Response> returnsFluxWithResponseWithString() {
            return Flux.just(Response.ok("test"));
        }

        Flux<Response> returnsNullFlux() {
            return null;
        }

        Flux<ServerSentEvent<ResponseDto>> returnsFluxOfServerSentEventsWithResponseDto() {
            return Flux.just(
                    ServerSentEvent.<ResponseDto>builder()
                            .data(responseDto)
                            .build()
            );
        }

        Response returnsNonPublisher() {
            return response;
        }
    }

    private class NotAnnotatedAsController {
        Mono<Response> returnsMonoWithResponse() {
            return Mono.just(response);
        }

        Flux<Response> returnsFluxWithResponse() {
            return Flux.just(response);
        }
    }
}