package com.example.sprintsight.ApiTests;

import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class BaseIntegrationTest {

    @LocalServerPort
    int port;

    String jwtCookie;
    String refreshCookie;
    UUID userUUID;

     final String JWT_COOKIE_NAME = "SprintSightJwtCookie";
     final String REFRESH_COOKIE_NAME = "SprintSightJwtRefreshCookie";




    @BeforeAll
    void setup() {
        String username = "user_" + UUID.randomUUID();
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api";

        // Signup
        given()
                .contentType("application/json")
                .body("""
        {
            "username": "%s",
            "password": "123456789",
            "email":"%s@mail.com",
            "fullName": "Test User"
        }
        """.formatted(username, username))
                .when()
                .post("/auth/signup")
                .then()
                .statusCode(anyOf(is(200), is(201)));


        var response =
                given()
                        .contentType("application/json")
                        .body("""
            {
                "username": "%s",
                "password": "123456789"
            }
            """.formatted(username))
                        .when()
                        .post("/auth/login")
                        .then()
                        .statusCode(200)
                        .extract().response();

        jwtCookie = response.getCookie(JWT_COOKIE_NAME);
        refreshCookie = response.getCookie(REFRESH_COOKIE_NAME);

        if (jwtCookie == null || refreshCookie == null) {
            throw new RuntimeException("Authentication failed: cookies not returned");
        }

        var body = response.as(new TypeRef<ApiResponse<UserResponse>>() {});
        userUUID = body.data().id();
    }

    @AfterAll
    void clean(){
        try {
            given()
                    .cookie(JWT_COOKIE_NAME, jwtCookie)
                    .cookie(REFRESH_COOKIE_NAME, refreshCookie)
                    .when()
                    .delete("/users/" + userUUID)
                    .then()
                    .statusCode(anyOf(is(200), is(204)));
        } catch (Exception e) {
            System.err.println("Failed to delete user: " + e.getMessage());
        }


    }
}
