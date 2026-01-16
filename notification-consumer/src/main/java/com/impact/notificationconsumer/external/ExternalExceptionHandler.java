package com.impact.notificationconsumer.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Slf4j
public class ExternalExceptionHandler {

    private ExternalExceptionHandler() {
    }


    public static Mono<Throwable> logAndSuppress(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("No response body")
                .doOnNext(body ->
                        log.error("External API error [{}]: {}", response.statusCode(), body)
                )
                .then(Mono.empty()); // 🚫 NO exception
    }

    /**
     * Retry only on timeouts or 5xx
     */
    public static boolean isRetryable(Throwable throwable) {
        return throwable instanceof TimeoutException
                || throwable instanceof WebClientResponseException.InternalServerError
                || throwable instanceof WebClientResponseException.BadGateway;
    }

}
