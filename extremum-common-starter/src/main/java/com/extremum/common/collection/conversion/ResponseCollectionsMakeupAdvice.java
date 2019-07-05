package com.extremum.common.collection.conversion;

import com.extremum.sharedmodels.dto.ResponseDto;
import com.extremum.common.response.Response;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author rpuch
 */
@ControllerAdvice
public class ResponseCollectionsMakeupAdvice implements ResponseBodyAdvice<Response> {
    private final CollectionMakeup makeup;

    public ResponseCollectionsMakeupAdvice(CollectionMakeup makeup) {
        this.makeup = makeup;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType() == Response.class;
    }

    @Override
    public Response beforeBodyWrite(Response body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        applyIfNeeded(body);
        return body;
    }

    private void applyIfNeeded(Response response) {
        if (response == null) {
            return;
        }

        applyMakeupToPayloadIfNeeded(response.getResult());
    }

    private void applyMakeupToPayloadIfNeeded(Object result) {
        if (result == null) {
            return;
        }

        if (result instanceof ResponseDto) {
            makeup.applyCollectionMakeup((ResponseDto) result);
        }

        if (result instanceof ResponseDto[]) {
            applyToArrayOfDto((ResponseDto[]) result);
        }

        if (result instanceof Iterable) {
            applyToIterable((Iterable<?>) result);
        }
    }

    private void applyToArrayOfDto(ResponseDto[] array) {
        for (ResponseDto dto : array) {
            makeup.applyCollectionMakeup(dto);
        }
    }

    private void applyToIterable(Iterable<?> iterable) {
        iterable.forEach(element -> {
            if (element instanceof ResponseDto) {
                makeup.applyCollectionMakeup((ResponseDto) element);
            }
        });
    }
}
