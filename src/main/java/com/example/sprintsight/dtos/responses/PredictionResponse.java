package com.example.sprintsight.dtos.responses;

public record PredictionResponse(
        double productivity,
        double quality,
        String productivityLabel,
        String qualityLabel
) {}
