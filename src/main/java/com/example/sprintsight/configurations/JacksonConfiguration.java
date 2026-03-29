package com.example.sprintsight.configurations;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    @Bean
    public org.openapitools.jackson.nullable.JsonNullableModule jsonNullableModule() {
        return new org.openapitools.jackson.nullable.JsonNullableModule();
    }
}
