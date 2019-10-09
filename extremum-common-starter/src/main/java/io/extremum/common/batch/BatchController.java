package io.extremum.common.batch;

import io.extremum.sharedmodels.dto.Response;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.ConnectionProvider;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Slf4j
@RestController
@RequestMapping("/batch")
public class BatchController {
    private WebClient webClient;
    private Scheduler resultScheduler;

    public BatchController(WebClient.Builder webClientBuilder) {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        // TODO refactor magic number to property variable
        factory.setLoopResources(useNative -> new NioEventLoopGroup(10, new DefaultThreadFactory("batch-thread")));
        factory.setConnectionProvider(ConnectionProvider.elastic("batch-client"));
        factory.setUseGlobalResources(false);

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(factory, Function.identity()))
                .build();
        // TODO refactor magic number to property variable
        this.resultScheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(10));
    }

    /*
     TODO
     * Next update's:
     - separate between our controllers (call inner-services) and other API (call by request)
     - add subscriber context for identify request
     */
    @PostMapping
    public Mono<List<Response>> submitBatch(@RequestBody BatchRequestDto[] batchDtos) {
        return Flux.fromArray(batchDtos)
                .flatMap(this::sendRequest)
                .publishOn(resultScheduler)
                .flatMap(this::wrapResponses)
                .collectList();
    }

    private Mono<ClientResponse> sendRequest(BatchRequestDto dto) {
        WebClient.RequestBodySpec request = webClient
                .method(dto.getMethod())
                .uri(dto.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON);

        if (dto.getBody() != null) {
            request.body(BodyInserters.fromObject(dto.getBody()));
        }
        return request.exchange();
    }

    private Flux<Response> wrapResponses(ClientResponse response) {
        if (response.statusCode().is1xxInformational()) {
            return ResponseWrapper.onInformational(response);
        } else if (response.statusCode().is2xxSuccessful()) {
            return ResponseWrapper.onSuccess(response);
        } else if (response.statusCode().is3xxRedirection()) {
            return ResponseWrapper.onRedirection(response);
        } else {
            return ResponseWrapper.onError(response);
        }
    }
}
