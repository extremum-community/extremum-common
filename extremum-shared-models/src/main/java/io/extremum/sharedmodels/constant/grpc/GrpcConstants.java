package io.extremum.sharedmodels.constant.grpc;

import com.google.common.net.HttpHeaders;
import io.grpc.Metadata;

public class GrpcConstants {
    public GrpcConstants() {
        throw new AssertionError();
    }

    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of(HttpHeaders.AUTHORIZATION, Metadata.ASCII_STRING_MARSHALLER);
    public static final String BEARER_TOKEN_TYPE = "Bearer ";

}
