package com.extremum.common.collection.conversion;

import com.extremum.common.dto.AbstractResponseDto;
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
        if (!(response.getResult() instanceof AbstractResponseDto)) {
            return;
        }

        makeup.applyCollectionMakeup((AbstractResponseDto) response.getResult());
    }
}
