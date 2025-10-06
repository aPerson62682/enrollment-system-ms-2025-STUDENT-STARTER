package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.exceptionhandling.HttpErrorInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static reactor.core.publisher.Operators.as;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

class CourseControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CourseRepository courseRepository;

    private final Long dbSize = 1000L;
    private final String NON_EXISTING_COURSE_ID = "11111111-2222-3333-4444-555555555555";
    private final String INVALID_COURSE_ID = "This-Is-An-Invalid-UUID";

    private String existingCourseId;
    private Course existingCourse;

    @BeforeEach
    public void dbSetup(){
        StepVerifier
                .create(courseRepository.count())
                .consumeNextWith(count -> {
                    assertEquals(dbSize, count);
                })
                .verifyComplete();

        StepVerifier.create(courseRepository.findAll().take(1))
                .consumeNextWith(course -> {
                    existingCourse = course;
                    existingCourseId = course.getCourseId();
                })
                .verifyComplete();
    }

    @Test
    public void getAllCoursesEventStream(){
        webTestClient.get()
                .uri("/api/v1/courses")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CourseResponseModel.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextCount(dbSize)
                .verifyComplete();
    }

    @Test
    public void whenGetCourseByCourseIdWithExistingCourseId_thenReturnCourseResponseModel(){
        //arrange
        Mono.from(courseRepository.findAll()
                .take(1))
                .doOnNext(course -> {
                    existingCourse = course;
                    existingCourseId = course.getCourseId();
                })
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();


        webTestClient.get()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.courseId").isEqualTo(existingCourse.getCourseId())
                .jsonPath("$.courseName").isEqualTo(existingCourse.getCourseName())
                .jsonPath("$.courseNumber").isEqualTo(existingCourse.getCourseNumber());

    }

    @Test
    public void whenGetCourseByCourseId_withInvalidCourseId_thenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri("/api/v1/courses/{courseId}", INVALID_COURSE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertEquals("Course id=" + INVALID_COURSE_ID + " is invalid", error.getMessage());
                });
    }


    @Test
    public void whenNewCourseWithValidRequestBody_shouldReturnSuccess() {
            CourseRequestModel courseRequestModel = new CourseRequestModel(
                    "cat-123",
                    "Web Services Testing",
                    45,
                    5.0,
                    "Computer Science"

            );

            webTestClient
                    .post()
                    .uri("/api/v1/courses")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(courseRequestModel)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(CourseResponseModel.class)
                    .value(courseResponseModel -> {
                        assertNotNull(courseResponseModel);
                        assertNotNull(courseResponseModel.courseId());
                        assertEquals(courseRequestModel.courseName(), courseResponseModel.courseName());
                        assertEquals(courseRequestModel.courseNumber(), courseResponseModel.courseNumber());
                    });
        }


    @Test
    public void whenAddNewCourseWithMissingCourseName_shouldReturnUnprocessableEntity(){
        var courseRequestModel = this.resourceToString("courseRequestModel-missing-courseName-422.json");

        webTestClient.post()
                .uri("/api/v1/courses")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals("Course name is required", errorInfo.getMessage());
                });
    }



    @Test
    public void whenGetCourseByCourseIdWithNonExistingCourseId_thenReturnNotFound() {
        webTestClient.get()
                .uri("/api/v1/courses/{courseId}", NON_EXISTING_COURSE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertEquals("Course with id=" + NON_EXISTING_COURSE_ID + " is not found", error.getMessage());
                    assertEquals("/api/v1/courses/" + NON_EXISTING_COURSE_ID, error.getPath());
                });
    }


    @Test
    public void whenUpdateCourseWithValidRequest_thenReturnUpdatedCourse() {
        CourseRequestModel updatedRequest = new CourseRequestModel(
                "updated", "Updated Name", 50, 3.0, "Updated Department");

        webTestClient.put()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseModel.class)
                .value(response -> {
                    assertEquals(existingCourseId, response.courseId());
                    assertEquals(updatedRequest.courseName(), response.courseName());
                    assertEquals(updatedRequest.courseNumber(), response.courseNumber());
                });
    }

    @Test
    public void whenUpdateCourseWithInvalidId_thenReturnUnprocessableEntity() {
        CourseRequestModel updatedRequest = new CourseRequestModel(
                "updated", "Updated Name", 50, 3.0, "Updated Department");

        webTestClient.put()
                .uri("/api/v1/courses/{courseId}", INVALID_COURSE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenUpdateCourseWithInvalidBody_thenReturnUnprocessableEntity() {
        CourseRequestModel invalidRequest = new CourseRequestModel(
                "short", null, -10, 0.0, null);

        webTestClient.put()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }



    @Test
    public void whenUpdateCourseWithNonExistingId_thenReturnNotFound() {
        CourseRequestModel updatedRequest = new CourseRequestModel(
                "updated", "Updated Name", 75, 6.0, "Updated Department");

        webTestClient.put()
                .uri("/api/v1/courses/{courseId}", NON_EXISTING_COURSE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRequest)
                .exchange()
                .expectStatus().isNotFound();
    }



    @Test
    public void whenDeleteCourseWithExistingId_thenReturnOk() {
        webTestClient.delete()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseModel.class)
                .value(response -> assertEquals(existingCourseId, response.courseId()));

        webTestClient.get()
                .uri("/api/v1/courses/{courseId}", existingCourseId)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    public void whenDeleteCourseWithNonExistingId_thenReturnNotFound() {
        webTestClient.delete()
                .uri("/api/v1/courses/{courseId}", NON_EXISTING_COURSE_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenDeleteCourseWithInvalidId_thenReturnUnprocessableEntity() {
        webTestClient.delete()
                .uri("/api/v1/courses/{courseId}", INVALID_COURSE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    protected String resourceToString(String relativePath){
        final Path TEST_RESOURCES_PATH = Path.of("src/test/resources");

        try {
            return Files.readString(TEST_RESOURCES_PATH.resolve(relativePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}