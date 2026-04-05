package com.example.sprintsight.ApiTests;


import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.entities.UserRole;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AuthApiTest {

    @LocalServerPort
    int port;

    String jwtCookie;
    String refreshCookie;
    UUID userUUID;

    @BeforeAll
    void setup() {
        RestAssured.port = port;

        // Register user (safe to call multiple times if handled)
        given()
                .contentType("application/json")
                .body("""
            {
                "username": "ahmed",
                "password": "123456789",
                "email":"ahmed@mail.com",
                "fullName": "ahmed ahmed"
            }
        """)
                .when()
                .post("/api/auth/signup");

        // Login once
        var response =
                given()
                        .contentType("application/json")
                        .body("""
                {
                    "username": "ahmed",
                    "password": "123456789"
                }
            """)
                        .when()
                        .post("/api/auth/login");

        var body = response.then().extract().as(new TypeRef<ApiResponse<UserResponse>>() {});

        jwtCookie = response.getCookie("SprintSightJwtCookie");
        refreshCookie = response.getCookie("SprintSightJwtRefreshCookie");
        UserResponse user =  body.data();
        userUUID = user.id();

    }


    @Test
    void shouldAccessProtectedEndpoint() {
        var response = given()
                .cookie("SprintSightJwtCookie", jwtCookie)
                .cookie("SprintSightJwtRefreshCookie",refreshCookie)
                .contentType("application/json")
                .body("""
                {
                    "bio": "this is my bio"
                }
            """)
                .when()
                .patch("/api/users/" + userUUID)
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<ApiResponse<UserResponse>>() {});


        assertThat(response.message(), equalTo("User updated successfully"));


        UserResponse user = (UserResponse) response.data();
        assertThat(user.id(), equalTo(userUUID));
        assertThat(user.bio(), equalTo("this is my bio"));
        assertThat(user.username(), equalTo("ahmed"));
        assertThat(user.email(),equalTo("ahmed@mail.com"));
        assertThat(user.fullName(),equalTo("ahmed ahmed"));
        assertThat(user.userRole(),equalTo(UserRole.USER));

    }

//    @Test
//    void shouldRefreshToken() {
//        var response =
//                given()
//                        .cookie("refresh", refreshCookie)
//                        .when()
//                        .post("/api/auth/refresh");
//
//        response.then().statusCode(200);
//
//        String newJwt = response.getCookie("jwt");
//
//        assertNotNull(newJwt);
//    }
}