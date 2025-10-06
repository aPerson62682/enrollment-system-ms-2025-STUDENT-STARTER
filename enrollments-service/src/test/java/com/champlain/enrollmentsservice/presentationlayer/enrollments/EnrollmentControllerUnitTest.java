package com.champlain.enrollmentsservice.presentationlayer.enrollments;

import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentService;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseResponseModel;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.EnrollmentNotFoundException;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.InvalidEnrollmentIdException;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.InvalidStudentIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrollmentControllerUnitTest {


    @InjectMocks
    private EnrollmentController enrollmentController;

    @Mock
    private EnrollmentService enrollmentService;

    private final String NON_EXISTING_ENROLLMENT_ID = "c3540a89-cb47-4c96-888e-ff96708db400";
    private final String EXISTING_ENROLLMENT_ID= "bbbbbbbb-cb47-4c96-888e-ff96708db400";
    private final String INVALID_ENROLLMENT_ID= "cb47-4c96-888e-ff96708db400";


    @Test
    public void whenGetEnrollmentByEnrollmentId_withNonExistingEnrollmentId_ThenThrowEnrollmentNotFoundException(){
        when(enrollmentService.getEnrollmentByEnrollmentId(NON_EXISTING_ENROLLMENT_ID))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<EnrollmentResponseModel>> result = enrollmentController.getEnrollmentByEnrollmentId(NON_EXISTING_ENROLLMENT_ID);

        StepVerifier
                .create(result)
                .expectErrorMatches(e -> e instanceof EnrollmentNotFoundException && e.getMessage().equals("Enrollment with id=" + NON_EXISTING_ENROLLMENT_ID + " is not found"))
                .verify();

    }



    @Test
    public void whenGetEnrollmentByEnrollmentId_withInvalidEnrollmentId_ThenThrowInvalidEnrollmentIdException(){
        Mono<ResponseEntity<EnrollmentResponseModel>> result = enrollmentController.getEnrollmentByEnrollmentId(INVALID_ENROLLMENT_ID);

        StepVerifier
                .create(result)
                .expectErrorMatches(e -> e instanceof InvalidEnrollmentIdException && e.getMessage().equals("Enrollment id=" + INVALID_ENROLLMENT_ID + " is invalid"))
                .verify();

    }


    @Test
    public void whenUpdateEnrollment_withInvalidEnrollmentId_thenThrowInvalidEnrollmentIdException() {
        // arrange
        EnrollmentRequestModel enrollmentRequestModel = new EnrollmentRequestModel(
                2024, Semester.FALL, UUID.randomUUID().toString(), UUID.randomUUID().toString());

        // act
        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.updateEnrollment(Mono.just(enrollmentRequestModel), INVALID_ENROLLMENT_ID);

        // assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidEnrollmentIdException &&
                        e.getMessage().equals("Enrollment id=" + INVALID_ENROLLMENT_ID + " is invalid"))
                .verify();
    }


    @Test
    public void whenDeleteEnrollment_withInvalidEnrollmentId_thenThrowInvalidEnrollmentIdException() {
        // act
        Mono<ResponseEntity<EnrollmentResponseModel>> result =
                enrollmentController.deleteEnrollment(INVALID_ENROLLMENT_ID);

        // assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidEnrollmentIdException &&
                        e.getMessage().equals("Enrollment id=" + INVALID_ENROLLMENT_ID + " is invalid"))
                .verify();
    }


    @Test
    public void whenGetEnrollmentByEnrollmentId_thenReturnEnrollment(){
        EnrollmentResponseModel enrollmentResponseModel = new EnrollmentResponseModel(
                EXISTING_ENROLLMENT_ID,
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                "Christopher",
                "Hernandez-Dauplo",
                UUID.randomUUID().toString(),
                "WEB-067",
                "WEB SERVICES"
        );
        when(enrollmentService.getEnrollmentByEnrollmentId(EXISTING_ENROLLMENT_ID)).
                thenReturn(Mono.just(enrollmentResponseModel));

        Mono<EnrollmentResponseModel> result = enrollmentService.getEnrollmentByEnrollmentId(EXISTING_ENROLLMENT_ID);

        StepVerifier
                .create(result)
                .expectNextMatches(enrollmentResponseModel1 -> enrollmentResponseModel1.enrollmentId().equals(EXISTING_ENROLLMENT_ID))
                .verifyComplete();
    }


    @Test
    public void whenAddEnrollment_thenReturnAddedEnrollment(){
        EnrollmentResponseModel enrollmentResponseModel = new EnrollmentResponseModel(
                EXISTING_ENROLLMENT_ID,
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                "Christopher",
                "Hernandez-Dauplo",
                UUID.randomUUID().toString(),
                "WEB-067",
                "WEB SERVICES"
        );
        EnrollmentRequestModel enrollmentRequestModel = new EnrollmentRequestModel(
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        when(enrollmentService.addEnrollment(any())).
                thenReturn(Mono.just(enrollmentResponseModel));

        Mono<EnrollmentResponseModel> result = enrollmentService.addEnrollment(Mono.just(enrollmentRequestModel));

        StepVerifier
                .create(result)
                .expectNextMatches(enrollmentResponseModel1 -> enrollmentResponseModel1.enrollmentId().equals(EXISTING_ENROLLMENT_ID))
                .verifyComplete();
    }


    @Test
    public void whenUpdateEnrollment_thenReturnUpdatedEnrollment(){
        EnrollmentResponseModel enrollmentResponseModel = new EnrollmentResponseModel(
                EXISTING_ENROLLMENT_ID,
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                "Christopher",
                "Hernandez-Dauplo",
                UUID.randomUUID().toString(),
                "WEB-067",
                "WEB SERVICES"
        );
        EnrollmentRequestModel enrollmentRequestModel = new EnrollmentRequestModel(
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        Mono<ResponseEntity<EnrollmentResponseModel>> result = Mono.just(ResponseEntity.ok(enrollmentResponseModel));
        when(enrollmentService.updateEnrollment(any(Mono.class), anyString()))
                .thenReturn(Mono.just(enrollmentResponseModel));

        Mono<ResponseEntity<EnrollmentResponseModel>> returnValue = enrollmentController.updateEnrollment(Mono.just(enrollmentRequestModel), EXISTING_ENROLLMENT_ID);

        StepVerifier
                .create(returnValue)
                .expectNextMatches(enrollmentResponseModel1 -> enrollmentResponseModel1.getBody().enrollmentId().equals(EXISTING_ENROLLMENT_ID))
                .verifyComplete();
    }


    @Test
    public void whenDeleteEnrollment_thenReturnDeletedEnrollment(){
        EnrollmentResponseModel enrollmentResponseModel = new EnrollmentResponseModel(
                EXISTING_ENROLLMENT_ID,
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                "Christopher",
                "Hernandez-Dauplo",
                UUID.randomUUID().toString(),
                "WEB-067",
                "WEB SERVICES"
        );

        when(enrollmentService.deleteEnrollmentByEnrollmentId(anyString()))
                .thenReturn(Mono.just(enrollmentResponseModel));

        Mono<ResponseEntity<EnrollmentResponseModel>> result = enrollmentController.deleteEnrollment(EXISTING_ENROLLMENT_ID);

        StepVerifier
                .create(result)
                .assertNext(responseEntity -> {
                    assertNotNull(responseEntity);
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(EXISTING_ENROLLMENT_ID, responseEntity.getBody().enrollmentId());
                    assertEquals(2000, responseEntity.getBody().enrollmentYear());
                    assertEquals(Semester.FALL, responseEntity.getBody().semester());
                    assertEquals("Christopher", responseEntity.getBody().studentFirstName());
                    assertEquals("Hernandez-Dauplo", responseEntity.getBody().studentLastName());
                    assertEquals("WEB-067", responseEntity.getBody().courseNumber());
                    assertEquals("WEB SERVICES", responseEntity.getBody().courseName());
                })
                .verifyComplete();
    }


    @Test
    public void whenGetAllCourses_thenReturnAllCourses() {
        //arrange
        EnrollmentResponseModel enrollmentResponseModel1 = new EnrollmentResponseModel(
                EXISTING_ENROLLMENT_ID,
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                "Christopher",
                "Hernandez-Dauplo",
                UUID.randomUUID().toString(),
                "WEB-067",
                "WEB SERVICES"
        );
        EnrollmentResponseModel enrollmentResponseModel2 = new EnrollmentResponseModel(
                EXISTING_ENROLLMENT_ID,
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                "Christopher",
                "Hernandez-Dauplo",
                UUID.randomUUID().toString(),
                "WEB-067",
                "WEB SERVICES"
        );
        EnrollmentResponseModel enrollmentResponseModel3 = new EnrollmentResponseModel(
                EXISTING_ENROLLMENT_ID,
                2000,
                Semester.FALL,
                UUID.randomUUID().toString(),
                "Christopher",
                "Hernandez-Dauplo",
                UUID.randomUUID().toString(),
                "WEB-067",
                "WEB SERVICES"
        );
        when(enrollmentService.getAllEnrollments()).thenReturn(Flux.just(enrollmentResponseModel1, enrollmentResponseModel2, enrollmentResponseModel3));
        //act
        Flux<EnrollmentResponseModel> result = enrollmentController.getAllEnrollments();

        //assert
        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
    }



}