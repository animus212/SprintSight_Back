package com.example.sprintsight.ApiTests;


import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.entities.UserRole;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserControllerTest extends BaseIntegrationTest{




    @Test
    void shouldUpdatePatchUser() {
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