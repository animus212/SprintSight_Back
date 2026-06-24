package com.example.sprintsight.clients;

import com.example.sprintsight.dtos.responses.PredictionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Slf4j
@Component
public class PredictionServiceClient {
    private final WebClient webClient;

    public PredictionServiceClient(@Value("${services.prediction-service.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public PredictionResponse getPrediction(UUID sprintId, String featuresJson) {
        try {
            return webClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(featuresJson)
                    .retrieve()
                    .bodyToMono(PredictionResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to get prediction for sprint {}: {} - {}",
                    sprintId, e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Cannot get prediction: " + e.getMessage(), e);
        }
    }
}
