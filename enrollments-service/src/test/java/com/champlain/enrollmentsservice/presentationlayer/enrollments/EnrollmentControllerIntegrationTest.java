package com.champlain.enrollmentsservice.presentationlayer.enrollments;

import com.champlain.enrollmentsservice.TestData;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseResponseModel;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnrollmentControllerIntegrationTest extends AbstractIntegrationClass {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();


    // Non-Mutating Tests First
    @Test
    @Order(1)
    public void whenGetAllEnrollments_thenReturnEnrollments() {
        webTestClient.get()
                .uri("/api/v1/enrollments")
                .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(EnrollmentResponseModel.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextCount(testData.dbSize)
                .verifyComplete();
    }


    @Test
    @Order(1)
    public void whenAddEnrollment_withNonExistingCourseId_thenReturnNotFound() {
        try {
            mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
            mockGetCourseByCourseIdException(TestData.NON_EXISTING_COURSEID, 404);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment_withNonExistingCourseId_RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Course with id=" +
                        TestData.NON_EXISTING_COURSEID + " is not found");
    }


    @Test
    @Order(2)
    public void whenAddEnrollment_withNonExistingStudentId_thenReturnNotFound() {
        webTestClient.post()

                .uri("/api/v1/enrollments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment_withNonExistingStudentId_RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Student with id=" + TestData.NON_EXISTING_STUDENTID + " is not found");
    }



    @Test
    @Order(3)
    public void whenAddEnrollment_withInvalidCourseId_thenReturnUnprocessableEntity() {
        try {
            mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
            mockGetCourseByCourseIdException(TestData.INVALID_COURSEID, HttpStatus.UNPROCESSABLE_ENTITY.value());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment_withInvalidCourseId_RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Course id=" + TestData.INVALID_COURSEID + " is invalid");
    }


    @Test
    @Order(4)
    public void whenAddEnrollment_withInvalidStudentId_thenReturnUnprocessableEntity() {
        // arrange
        try {
            mockGetStudentByStudentIdException(TestData.INVALID_STUDENTID, HttpStatus.UNPROCESSABLE_ENTITY.value());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // act & assert
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(testData.enrollment_withInvalidStudentId_RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Student id=" + TestData.INVALID_STUDENTID + " is invalid");
    }


    // Mutating Tests at the end
    @Test
    @Order(5)
    public void whenAddValidEnrollmentRequest_thenReturnEnrollmentResponseModel() {
        //arrange
        try {
            mockGetStudentByStudentIdSuccess(testData.student1ResponseModel);
            mockGetCourseByCourseIdSuccess(testData.course1ResponseModel);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //act
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .body(Mono.just(testData.enrollment1RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseModel.class)
                .value(enrollmentResponseModel -> {
                    assertNotNull(enrollmentResponseModel);
                    assertNotNull(enrollmentResponseModel.enrollmentId());
                    assertEquals(testData.enrollment1RequestModel.enrollmentYear(), enrollmentResponseModel.enrollmentYear());
                });
        StepVerifier.create(enrollmentRepository.count())
                .expectNext(testData.dbSize + 1)
                .verifyComplete();
    }


    private void mockGetCourseByCourseIdSuccess(CourseResponseModel model) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(model);

        mockServerClient.when(HttpRequest.request("/api/v1/courses/" + model.courseId()))
                .respond(
                        HttpResponse.response(jsonBody)
                                .withStatusCode(200)
                                .withContentType(MediaType.APPLICATION_JSON)
                );
    }


    private void mockGetStudentByStudentIdSuccess(StudentResponseModel model) throws JsonProcessingException {
        String jsonBody = objectMapper.writeValueAsString(model);

        mockServerClient.when(HttpRequest.request("/api/v1/students/" + model.studentId()))
                .respond(
                        HttpResponse.response(jsonBody)
                                .withStatusCode(200)
                                .withContentType(MediaType.APPLICATION_JSON)
                );
    }


    private void mockGetCourseByCourseIdException(String courseId, int responseCode) {
        mockServerClient.when(HttpRequest.request("/api/v1/courses/" + courseId))
                .respond(
                        HttpResponse.response()
                                .withStatusCode(responseCode)
                                .withContentType(MediaType.APPLICATION_JSON)
                );
    }


    private void mockGetStudentByStudentIdException(String studentId, int responseCode) {
        mockServerClient.when(HttpRequest.request("/api/v1/students/" + studentId))
                .respond(
                        HttpResponse.response()
                                .withStatusCode(responseCode)
                                .withContentType(MediaType.APPLICATION_JSON)
                );


    }




}