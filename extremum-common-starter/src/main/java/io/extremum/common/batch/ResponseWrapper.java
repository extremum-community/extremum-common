package io.extremum.common.batch;

import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ResponseWrapper {

    static Flux<Response> onInformational(ClientResponse response, String id) {
        return Flux.from(response.bodyToMono(Response.class)
                .map(body -> Response.builder()
                        .withRequestId(id)
                        .withOkStatus(response.statusCode().value())
                        .withAlert(Alert.infoAlert("Informational request"))
                        .build()));
    }

    static Flux<Response> onSuccess(ClientResponse response, String id) {
        return response.bodyToFlux(Response.class)
                .map(body -> Response.builder(body)
                        .withRequestId(id)
                        .build());
    }

    static Flux<Response> onRedirection(ClientResponse response, String id) {
        return Flux.from(response.bodyToMono(Response.class)
                .map(body -> Response.builder()
                        .withRequestId(id)
                        .withWarningStatus(response.statusCode().value())
                        .withAlert(Alert.warningAlert("Request redirected"))
                        .build()));
    }

    static Flux<Response> onError(ClientResponse response, String id) {
        if (response.statusCode().is4xxClientError()) {
            if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                return Flux.from(errorResponse("Not found", id, response));
            } else {
                return Flux.from(errorResponse("Bad request", id, response));
            }
        } else {
            return Flux.from(errorResponse("Internal server error", id, response));
        }
    }

    private static Mono<Response> errorResponse(String message, String id, ClientResponse clientResponse) {
        return clientResponse.bodyToMono(Response.class)
                .map(body -> Response.builder()
                        .withRequestId(id)
                        .withFailStatus(clientResponse.statusCode().value())
                        .withAlert(Alert.errorAlert(message))
                        .build());
    }
}
