package io.extremum.common.batch;

import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ResponseWrapper {

    static Flux<Response> onInformational(ClientResponse response) {
        return Flux.from(response.bodyToMono(Response.class)
                .map(body -> Response.builder()
                        .withOkStatus(response.statusCode().value())
                        .withAlert(Alert.infoAlert("Informational request"))
                        .build()));
    }

    static Flux<Response> onSuccess(ClientResponse response) {
        return response.bodyToFlux(Response.class);
    }

    static Flux<Response> onRedirection(ClientResponse response) {
        return Flux.from(response.bodyToMono(Response.class)
                .map(body -> Response.builder()
                        .withWarningStatus(response.statusCode().value())
                        .withAlert(Alert.warningAlert("Request redirected"))
                        .build()));
    }

    static Flux<Response> onError(ClientResponse response) {
        if (response.statusCode().is4xxClientError()) {
            if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                return Flux.from(errorResponse("Not found", response));
            } else {
                return Flux.from(errorResponse("Bad request", response));
            }
        } else {
            return Flux.from(errorResponse("Internal server error", response));
        }
    }

    private static Mono<Response> errorResponse(String message, ClientResponse clientResponse) {
        return clientResponse.bodyToMono(Response.class)
                .map(body -> Response.builder()
                        .withFailStatus(clientResponse.statusCode().value())
                        .withAlert(Alert.errorAlert(message))
                        .build());
    }
}
