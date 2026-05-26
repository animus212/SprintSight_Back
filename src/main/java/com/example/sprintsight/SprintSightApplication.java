package com.example.sprintsight;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
public class SprintSightApplication {
    public static void main(String[] args) {
        loadDotenvIfDev();

        SpringApplication.run(SprintSightApplication.class, args);
    }

    private static void loadDotenvIfDev() {
        String activeProfile = System.getenv("ACTIVE_PROFILES");

        if (activeProfile == null) {
            activeProfile = System.getProperty("ACTIVE_PROFILES", "dev");
        }

        if ("prod".equalsIgnoreCase(activeProfile)) {
            return;
        }

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();

            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, entry.getValue());
            }
        });
    }
}
