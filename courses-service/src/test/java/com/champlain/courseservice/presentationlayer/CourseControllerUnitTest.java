package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.businesslayer.CourseService;
import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.exceptionhandling.exceptions.CourseNotFoundException;
import com.champlain.courseservice.exceptionhandling.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseControllerUnitTest {

    @InjectMocks
    private CourseController courseController;

    @Mock
    private CourseService courseService;


    private final String EXISTING_COURSE_ID = "a58f0966-a7f6-43b6-a468-9db098090949";
    private final String NON_EXISTING_COURSE_ID = "a58f0966-a7f6-43b6-a468-9db098090941";
    private final String INVALID_COURSE_ID = "a58f0966-a7f6-43b6-a468-9db098090949123";


    @Test
    public void whenGetCourseByCourseId_withNonExistingCourseId_thenThrowCourseNotFoundException(){
        //arrange
        when(courseService.getCourseByCourseId(NON_EXISTING_COURSE_ID))
                .thenReturn(Mono.empty());

        //act
        Mono<ResponseEntity<CourseResponseModel>> result = courseController
                .getCourseByCourseId(NON_EXISTING_COURSE_ID);

        //assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof CourseNotFoundException
                && e.getMessage().equals("Course with id=" + NON_EXISTING_COURSE_ID + " is not found"))
                .verify();
    }


    @Test
    public void whenAddCourse_withInvalidHours_thenThrowInvalidInputException(){
        CourseRequestModel courseRequestModel = new CourseRequestModel(
                "cat-123",
                "Web Services Testing",
                0,
                5.0,
                "Computer Science"
        );

        Mono<ResponseEntity<CourseResponseModel>> result = courseController.addCourse(Mono.just(courseRequestModel));

                // assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidInputException
                        && e.getMessage().equals("Course hours must be greater than 0"))
                .verify();
    }

    @Test
    public void whenUpdateCourse_withInvalidHours_thenThrowInvalidInputException(){
        CourseRequestModel courseRequestModel = new CourseRequestModel(
                "cat-123",
                "Web Services Testing",
                0,
                5.0,
                "Computer Science"
        );

        Mono<ResponseEntity<CourseResponseModel>> result = courseController.updateCourse(Mono.just(courseRequestModel), EXISTING_COURSE_ID);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidInputException
                        && e.getMessage().equals("Course hours must be greater than 0"))
                .verify();
    }




}

