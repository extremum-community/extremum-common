package io.extremum.elasticsearch.springdata.reactiverepository;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestStatus;
import org.reactivestreams.Publisher;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.ClientLogger;
import org.springframework.data.elasticsearch.client.reactive.*;
import org.springframework.data.elasticsearch.client.util.RequestConverters;
import org.springframework.data.util.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * NB: all this is needed just to pass if_seq_no and if_primary_term parameters to index request.
 */
public class ExtremumReactiveElasticsearchClient extends DefaultReactiveElasticsearchClient {
    public static ReactiveElasticsearchClient create(HttpHeaders headers, String... hosts) {
        Assert.notNull(headers, "HttpHeaders must not be null");
        Assert.notEmpty(hosts, "Elasticsearch Cluster needs to consist of at least one host");

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(hosts)
                .withDefaultHeaders(headers)
                .build();

        return create(clientConfiguration);
    }

    public static ReactiveElasticsearchClient create(ClientConfiguration clientConfiguration) {
        Assert.notNull(clientConfiguration, "ClientConfiguration must not be null");

        WebClientProvider provider = getWebClientProvider(clientConfiguration);
        HostProvider hostProvider = HostProvider.provider(provider, clientConfiguration.getEndpoints().toArray(new InetSocketAddress[0]));
        return new ExtremumReactiveElasticsearchClient(hostProvider);
    }

    private static WebClientProvider getWebClientProvider(ClientConfiguration clientConfiguration) {
        Duration connectTimeout = clientConfiguration.getConnectTimeout();
        Duration soTimeout = clientConfiguration.getSocketTimeout();
        TcpClient tcpClient = TcpClient.create();
        if (!connectTimeout.isNegative()) {
            tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()));
        }

        if (!soTimeout.isNegative()) {
            tcpClient = tcpClient.doOnConnected((connection) -> {
                connection.addHandlerLast(new ReadTimeoutHandler(soTimeout.toMillis(), TimeUnit.MILLISECONDS)).addHandlerLast(new WriteTimeoutHandler(soTimeout.toMillis(), TimeUnit.MILLISECONDS));
            });
        }

        String scheme = "http";
        HttpClient httpClient = HttpClient.from(tcpClient);
        if (clientConfiguration.useSsl()) {
            httpClient = httpClient.secure((sslConfig) -> {
                Optional<SSLContext> sslContext = clientConfiguration.getSslContext();
                sslContext.ifPresent((it) -> {
                    sslConfig.sslContext(new JdkSslContext(it, true, ClientAuth.NONE));
                });
            });
            scheme = "https";
        }

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        WebClientProvider provider = WebClientProvider.create(scheme, connector);
        return provider.withDefaultHeaders(clientConfiguration.getDefaultHeaders());
    }

    public ExtremumReactiveElasticsearchClient(HostProvider hostProvider) {
        super(hostProvider);
    }

    @Override
    public Mono<IndexResponse> index(HttpHeaders headers, IndexRequest indexRequest) {
        return sendRequest(indexRequest, this::convertIndex, IndexResponse.class, headers).publishNext();
    }

    private Request convertIndex(IndexRequest indexRequest) {
        Request result = RequestConverters.index(indexRequest);
        if (indexRequest.ifSeqNo() != SequenceNumbers.UNASSIGNED_SEQ_NO) {
//            result.addParameter("if_seq_no", Long.toString(indexRequest.ifSeqNo()));
        }
        if (indexRequest.ifPrimaryTerm() != SequenceNumbers.UNASSIGNED_PRIMARY_TERM) {
//            result.addParameter("if_primary_term", Long.toString(indexRequest.ifPrimaryTerm()));
        }
        return result;
    }

    private <Req extends ActionRequest, Resp extends ActionResponse> Flux<Resp> sendRequest(Req request,
                Function<Req, Request> converter, Class<Resp> responseType, HttpHeaders headers) {
        return sendRequest(converter.apply(request), responseType, headers);
    }

    private <AR extends ActionResponse> Flux<AR> sendRequest(Request request, Class<AR> responseType, HttpHeaders headers) {
        String logId = ClientLogger.newLogId();
        return execute((webClient) -> sendRequest(webClient, logId, request, headers))
                .flatMapMany((response) -> readResponseBody(logId, request, response, responseType));
    }

    private Mono<ClientResponse> sendRequest(WebClient webClient, String logId, Request request, HttpHeaders headers) {
        WebClient.RequestBodySpec requestBodySpec = webClient.method(HttpMethod.valueOf(request.getMethod().toUpperCase())).uri((builder) -> {
            builder = builder.path(request.getEndpoint());
            Map.Entry entry;
            if (!ObjectUtils.isEmpty(request.getParameters())) {
                for(Iterator var2 = request.getParameters().entrySet().iterator(); var2.hasNext(); builder = builder.queryParam((String)entry.getKey(), new Object[]{entry.getValue()})) {
                    entry = (Map.Entry)var2.next();
                }
            }

            return builder.build(new Object[0]);
        }).attribute(ClientRequest.LOG_ID_ATTRIBUTE, logId).headers((theHeaders) -> {
            theHeaders.addAll(headers);
            if (request.getOptions() != null && !ObjectUtils.isEmpty(request.getOptions().getHeaders())) {
                request.getOptions().getHeaders().forEach((it) -> {
                    theHeaders.add(it.getName(), it.getValue());
                });
            }

        });
        if (request.getEntity() != null) {
            Lazy<String> body = this.bodyExtractor(request);
            ClientLogger.logRequest(logId, request.getMethod().toUpperCase(), request.getEndpoint(), request.getParameters(), body::get);
            requestBodySpec.contentType(MediaType.valueOf(request.getEntity().getContentType().getValue()));
            requestBodySpec.body(Mono.fromSupplier(body::get), String.class);
        } else {
            ClientLogger.logRequest(logId, request.getMethod().toUpperCase(), request.getEndpoint(), request.getParameters());
        }

        return requestBodySpec.exchange().onErrorReturn(ConnectException.class, ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    private Lazy<String> bodyExtractor(Request request) {
        return Lazy.of(() -> {
            try {
                return EntityUtils.toString(request.getEntity());
            } catch (IOException var2) {
                throw new RequestBodyEncodingException("Error encoding request", var2);
            }
        });
    }

    private <T> Publisher<? extends T> readResponseBody(String logId, Request request, ClientResponse response, Class<T> responseType) {
        if (responseType.getName().endsWith(".RawActionResponse")) {
            ClientLogger.logRawResponse(logId, response.statusCode());
//            return Mono.just(responseType.cast(RawActionResponse.create(response)));
            throw new IllegalStateException("How should we deal with it?");
        } else if (response.statusCode().is5xxServerError()) {
            ClientLogger.logRawResponse(logId, response.statusCode());
            return handleServerError(request, response);
        } else {
            return (response.body(BodyExtractors.toMono(byte[].class))).map((it) -> {
                return new String(it, StandardCharsets.UTF_8);
            }).doOnNext((it) -> {
                ClientLogger.logResponse(logId, response.statusCode(), it);
            }).flatMap((content) -> {
                return doDecode(response, responseType, content);
            });
        }
    }

    private static <T> Mono<T> doDecode(ClientResponse response, Class<T> responseType, String content) {
        String mediaType = response.headers().contentType()
                .map(MimeType::toString)
                .orElse(XContentType.JSON.mediaType());

        try {
            Method fromXContent = ReflectionUtils.findMethod(responseType, "fromXContent", new Class[]{XContentParser.class});
            return Mono.justOrEmpty(responseType.cast(ReflectionUtils.invokeMethod(fromXContent, responseType, new Object[]{createParser(mediaType, content)})));
        } catch (Throwable var7) {
            try {
                return Mono.error(BytesRestResponse.errorFromXContent(createParser(mediaType, content)));
            } catch (Exception var6) {
                return Mono.error(new ElasticsearchStatusException(content, RestStatus.fromCode(response.statusCode().value()), new Object[0]));
            }
        }
    }

    private static XContentParser createParser(String mediaType, String content) throws IOException {
        return XContentType.fromMediaTypeOrFormat(mediaType).xContent().createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, content);
    }

    private static <T> Publisher<? extends T> handleServerError(Request request, ClientResponse response) {
        return Mono.error(new HttpServerErrorException(response.statusCode(), String.format("%s request to %s returned error code %s.", request.getMethod(), request.getEndpoint(), response.statusCode().value())));
    }
}
