package io.extremum.common.batch;

import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.ConnectionProvider;

import javax.validation.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@RestController
@RequestMapping("/v1/batch")
public class BatchController {
    private WebClient webClient;
    private Scheduler resultScheduler;
    private Validator validator;

    public BatchController(WebClient.Builder webClientBuilder) {
        ReactorResourceFactory resourceFactory = new ReactorResourceFactory();
        // TODO refactor magic number to property variable
        resourceFactory.setLoopResources(useNative -> new NioEventLoopGroup(10, new DefaultThreadFactory("batch-thread")));
        resourceFactory.setConnectionProvider(ConnectionProvider.elastic("batch-client"));
        resourceFactory.setUseGlobalResources(false);

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(resourceFactory, Function.identity()))
                .build();
        // TODO refactor magic number to property variable
        this.resultScheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(10));
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /*
     TODO
     * Next update's:
     - separate between our controllers (call inner-services) and other API (call by request)
     - add header separate
     */
    @PostMapping
    public Mono<List<Response>> submitBatch(@RequestBody BatchRequestDto[] batchDto, @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return Flux.fromArray(batchDto)
                .flatMap(requestDto -> sendRequest(requestDto, auth))
                .publishOn(resultScheduler)
                .flatMap(this::wrapResponses)
                .collectList();
    }

    private Mono<DataWithId> sendRequest(BatchRequestDto dto, String auth) {
        Set<ConstraintViolation<BatchRequestDto>> violations = validator.validate(dto);
        if (violations.size() != 0) {
            StringBuilder violationMessage = new StringBuilder();
            violations.forEach(violation -> violationMessage
                    .append(violation.getPropertyPath().toString())
                    .append(" - ")
                    .append(violation.getMessage())
                    .append(","));
            throw new ValidationException(violationMessage.toString().substring(0, violationMessage.length() - 2));
        }

        WebClient.RequestBodySpec request = webClient
                .method(dto.getMethod())
                .uri(uriBuilder -> uriBuilder
                        .path(dto.getEndpoint())
                        .query(dto.getQuery())
                        .build())
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON);

        if (dto.getBody() != null) {
            request.body(BodyInserters.fromObject(dto.getBody()));
        }
        return request.exchange()
                .map(response -> new DataWithId(dto.getId(), response));
    }

    private Flux<Response> wrapResponses(DataWithId data) {
        // This cast is safe because we put it on the previous step
        ClientResponse response = (ClientResponse) data.getData();
        String id = data.getId();

        if (response.statusCode().is1xxInformational()) {
            return ResponseWrapper.onInformational(response, id);
        } else if (response.statusCode().is2xxSuccessful()) {
            return ResponseWrapper.onSuccess(response, id);
        } else if (response.statusCode().is3xxRedirection()) {
            return ResponseWrapper.onRedirection(response, id);
        } else {
            return ResponseWrapper.onError(response, id);
        }
    }

    private static Mono<Response> onValidationException(ValidationException e) {
        return Mono.just(Response.fail(Alert.errorAlert(e.getMessage()), HttpStatus.BAD_REQUEST.value()));
    }
}
