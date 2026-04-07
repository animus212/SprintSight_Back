package com.example.sprintsight.ApiTests;

import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class ProjectControllerTest extends BaseIntegrationTest {
    UUID projectId;

    @Test
    @Order(1)
    void shouldCreateProject() {
        String requestBody = """
        {
            "name": "Test Project",
            "description": "Test Description"
        }
        """;

        var response = given()
                .cookie(JWT_COOKIE_NAME, jwtCookie)
                .cookie(REFRESH_COOKIE_NAME,refreshCookie)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/projects")
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<ApiResponse<ProjectResponse>>() {});

        assertThat(response.message(), equalTo("Project created successfully"));

        ProjectResponse project = response.data();
        assertThat(project.id(), notNullValue());
        assertThat(project.createdBy().id(), equalTo(userUUID));
        assertThat(project.name(), equalTo("Test Project"));
        assertThat(project.description(),equalTo("Test Description"));

        projectId = project.id();

    }

    @Test
    @Order(2)
    void shouldGetProjectById() {
       var response = given()
                .cookie(JWT_COOKIE_NAME, jwtCookie)
                .cookie(REFRESH_COOKIE_NAME,refreshCookie)
                .when()
                .get("/projects/"+ projectId)
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<ApiResponse<ProjectResponse>>() {});

        assertThat(response.message(), equalTo("Project retrieved successfully"));
        ProjectResponse project = response.data();
        assertThat(project.id(), notNullValue());
        assertThat(project.createdBy().id(), equalTo(userUUID));
        assertThat(project.name(), equalTo("Test Project"));
        assertThat(project.description(),equalTo("Test Description"));

    }

    @Test
    @Order(3)
    void shouldGetAllProjects() {
        given()
                .cookie(JWT_COOKIE_NAME, jwtCookie)
                .cookie(REFRESH_COOKIE_NAME,refreshCookie)
                .when()
                .get("/projects")
                .then()
                .statusCode(200)
                .body("data", notNullValue());
    }

    @Test
    @Order(4)
    void shouldUpdateProject_PUT() {


        String requestBody = """
        {
            "name": "Updated Project",
            "description": "Updated Description"
        }
        """;

       var response = given()
                .cookie(JWT_COOKIE_NAME, jwtCookie)
                .cookie(REFRESH_COOKIE_NAME,refreshCookie)
                .contentType(ContentType.JSON)
                .pathParam("id", projectId)
                .body(requestBody)
                .when()
                .put("/projects/{id}")
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<ApiResponse<ProjectResponse>>() {});


        assertThat(response.message(), equalTo("Project updated successfully"));
        ProjectResponse project = response.data();
        assertThat(project.id(), notNullValue());
        assertThat(project.createdBy().id(), equalTo(userUUID));
        assertThat(project.name(), equalTo("Updated Project"));
        assertThat(project.description(),equalTo("Updated Description"));
    }

    @Test
    @Order(5)
    void shouldUpdateProject_PATCH() {

        String requestBody = """
        {
            "name": "Patched Name"
        }
        """;

       var response = given()
               .cookie(JWT_COOKIE_NAME, jwtCookie)
               .cookie(REFRESH_COOKIE_NAME,refreshCookie)
                .contentType(ContentType.JSON)
                .pathParam("id", projectId)
                .body(requestBody)
                .when()
                .patch("/projects/{id}")
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<ApiResponse<ProjectResponse>>() {});

        assertThat(response.message(), equalTo("Project updated successfully"));
        ProjectResponse project = response.data();
        assertThat(project.id(), notNullValue());
        assertThat(project.createdBy().id(), equalTo(userUUID));
        assertThat(project.name(), equalTo("Patched Name"));
        assertThat(project.description(),equalTo("Updated Description"));
    }

    @Test
    @Order(6)
    void shouldDeleteProject() {
        try{
            given()
                    .cookie(JWT_COOKIE_NAME, jwtCookie)
                    .cookie(REFRESH_COOKIE_NAME,refreshCookie)
                .pathParam("id", projectId)
                .when()
                .delete("/projects/{id}")
                .then()
                .statusCode(anyOf(is(200), is(204)));
        }catch (Exception e){
            System.err.println("Failed to delete project: " + e.getMessage());
        }
    }
}
