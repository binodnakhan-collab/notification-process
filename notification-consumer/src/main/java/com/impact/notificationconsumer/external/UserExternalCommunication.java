package com.impact.notificationconsumer.external;

import com.impact.notificationconsumer.payload.response.ExternalUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserExternalCommunication {

    @Value("${external.host}")
    private String externalHost;

    private final WebClient webClient;

    public ExternalUserResponse getUserDetail(Long id) {
        return webClient.get()
                .uri(externalHost + "/metadata/{id}", id)
                .retrieve()
                .onStatus(status ->
                    status == HttpStatus.NOT_FOUND,
                    response -> {
                        log.warn("Something went wrong while calling external api.");
                        return Mono.empty();
                    }
                )
                .onStatus(HttpStatusCode::isError, ExternalExceptionHandler::logAndSuppress)
                .bodyToMono(ExternalUserResponse.class)
                .timeout(Duration.ofSeconds(3))
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(300))
                                .filter(ExternalExceptionHandler::isRetryable)
                                .doBeforeRetry(retrySignal -> {
                                    long attempt = retrySignal.totalRetries() + 1;
                                    Throwable failure = retrySignal.failure();
                                    log.warn("Retry attempt {} due to error: {}", attempt, failure.getMessage(), failure);
                                })
                )

                .onErrorResume(ex -> {
                    if (ex instanceof WebClientRequestException wc) {
                        log.error("Service unavailable at this moment please try again later.");
                    } else {
                        log.error("External api call failed for id={} with exception {}", id, ex.getMessage());
                    }
                    return Mono.empty();
                })
                .block();
    }


}
