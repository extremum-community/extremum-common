package io.extremum.batch.controller;

import io.extremum.batch.config.BatchProperties;
import io.extremum.batch.config.WebClientProperties;
import io.extremum.batch.model.BatchRequestDto;
import io.extremum.batch.model.ValidatedRequest;
import io.extremum.batch.utils.ResponseWrapper;
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

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static io.extremum.batch.utils.BatchValidation.validateRequest;

@Slf4j
@RestController
@RequestMapping("/v1/batch")
public class BatchController {
    private WebClient webClient;
    private Scheduler resultScheduler;
    private Validator validator;

    public BatchController(WebClientProperties clientProps, BatchProperties batchProps, WebClient.Builder webClientBuilder) {
        ReactorResourceFactory resourceFactory = new ReactorResourceFactory();
        resourceFactory.setLoopResources(useNative -> new NioEventLoopGroup(clientProps.getWorkerThreadSize(), new DefaultThreadFactory("batch-thread")));
        resourceFactory.setConnectionProvider(ConnectionProvider.elastic("batch-client"));
        resourceFactory.setUseGlobalResources(false);
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(resourceFactory, Function.identity()))
                .build();

        this.resultScheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(batchProps.getResultThreadSize()));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /*
     TODO
     * Next update's:
     - add header separate
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Response>> submitBatch(@RequestBody BatchRequestDto[] batchDto,
                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION,required = false) String auth) {
        return Flux.fromArray(batchDto)
                .flatMap(validateRequest(validator))
                .flatMap(validated -> {
                    if (validated.getEx() == null) {
                        return sendRequest((BatchRequestDto) validated.getData(), auth);
                    } else {
                        return Mono.just(validated);
                    }
                })
                .publishOn(resultScheduler)
                .flatMap(this::wrapResponses)
                .collectList();
    }

    private Mono<ValidatedRequest> sendRequest(BatchRequestDto dto, String auth) {
        WebClient.RequestBodySpec request = webClient
                .method(dto.getMethod())
                .uri(dto.getEndpoint() + Optional.ofNullable(dto.getQuery()).orElse(""))
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON);

        if (dto.getBody() != null) {
            request.body(BodyInserters.fromObject(dto.getBody()));
        }
        return request.exchange()
                .map(response -> new ValidatedRequest(dto.getId(), response));
    }

    private Flux<Response> wrapResponses(ValidatedRequest validated) {
        if (validated.getEx() == null) {
            // This cast is safe because we put it on the previous step
            ClientResponse response = (ClientResponse) validated.getData();
            String id = validated.getId();

            if (response.statusCode().is1xxInformational()) {
                return ResponseWrapper.onInformational(response, id);
            } else if (response.statusCode().is2xxSuccessful()) {
                return ResponseWrapper.onSuccess(response, id);
            } else if (response.statusCode().is3xxRedirection()) {
                return ResponseWrapper.onRedirection(response, id);
            } else {
                return ResponseWrapper.onError(response, id);
            }
        } else {
            String id = validated.getId();
            String violations = validated.getEx().getMessage();
            return Flux.just(Response.builder()
                    .withFailStatus(HttpStatus.BAD_REQUEST.value())
                    .withRequestId(id)
                    .withAlerts(Collections.singleton(Alert.errorAlert("Validation failed: " + violations)))
                    .withNowTimestamp()
                    .build());
        }
    }
}
