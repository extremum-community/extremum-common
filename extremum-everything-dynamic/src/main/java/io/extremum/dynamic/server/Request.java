package io.extremum.dynamic.server;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;

@Getter
public class Request {
    private final HttpMethod method;
    private final URI path;
    private final HttpHeaders headers;
    private final String proto;

    public Request(HttpMethod method, URI path, HttpHeaders headers, String proto) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.proto = proto;
    }

    public static RequestBuilder Builder() {
        return new RequestBuilder();
    }

    public static class RequestBuilder {
        private HttpMethod method;
        private URI path;
        private String proto;
        private HttpHeaders headers = new HttpHeaders();

        public Request build() {
            return new Request(method, path, headers, proto);
        }

        public void withProto(String protoData) {
            String[] splitted = protoData.split(" ");

            method = HttpMethod.valueOf(splitted[0]);
            path = URI.create(splitted[1]);
            proto = splitted[2];
        }

        public void withHeader(String line) {
            String[] splitted = line.split(":");
            headers.add(splitted[0], splitted[1].trim());
        }
    }

    @Override
    public String toString() {
        return "Request{" +
                "method=" + method +
                ", path=" + path +
                ", headers=" + headers +
                ", proto='" + proto + '\'' +
                '}';
    }
}
