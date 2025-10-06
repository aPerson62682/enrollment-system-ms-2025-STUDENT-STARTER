package com.champlain.courseservice.businesslayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.mapper.EntityModelMapper;
import com.champlain.courseservice.presentationlayer.CourseRequestModel;
import com.champlain.courseservice.presentationlayer.CourseResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static reactor.core.Disposables.never;

@ExtendWith(MockitoExtension.class)
class CourseServiceUnitTest {

    @InjectMocks
    private CourseServiceImpl courseService;

    @Mock
    private CourseRepository courseRepository;

    Course course1 = Course.builder()
            .id(1)
            .courseId(UUID.randomUUID().toString())
            .courseNumber("1cat-422")
            .courseName("Web Services 1")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course2 = Course.builder()
            .id(2)
            .courseId(UUID.randomUUID().toString())
            .courseNumber("2cat-423")
            .courseName("Web Services 2")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course3 = Course.builder()
            .id(3)
            .courseId(UUID.randomUUID().toString())
            .courseNumber("3cat-423")
            .courseName("Web Services 3")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    @Test
    public void whenGetAllCourses_thenReturnThreeCourses(){
        //arrange

        when(courseRepository.findAll())
                .thenReturn(Flux.just(course1, course2, course3));
        //act
        Flux<CourseResponseModel> result = courseService.getAllCourses();

        //assert
        StepVerifier
                .create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(courseResponseModel.courseNumber(), course1.getCourseNumber());
                    return true;
                })
                .expectNextMatches(courseResponseModel -> courseResponseModel.courseNumber().equals(course2.getCourseNumber()))
                .expectNextMatches(courseResponseModel -> courseResponseModel.courseNumber().equals(course3.getCourseNumber()))
                .verifyComplete();

    }


    @Test
    public void whenGetCourseByCourseId_withNonExistentId_thenReturnEmpty() {
        //arrange
        when(courseRepository.findCourseByCourseId(anyString()))
                .thenReturn(Mono.empty());

        //act
        Mono<CourseResponseModel> result = courseService.getCourseByCourseId("Non-Existent-Id");

        //assert
        StepVerifier
                .create(result)
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    void whenDeleteCourse_withValidId_thenReturnDeletedCourse() {
        //arrange
        when(courseRepository.findCourseByCourseId(course1.getCourseId())).thenReturn(Mono.just(course1));
        when(courseRepository.delete(course1)).thenReturn(Mono.empty());

        //act
        Mono<CourseResponseModel> result = courseService.deleteCourseByCourseId(course1.getCourseId());

        //assert
        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(course1.getCourseId(), response.courseId());
                    assertEquals(course1.getCourseName(), response.courseName());
                })
                .verifyComplete();
    }


    @Test
    public void whenGetCourseByCourseId_withValidId_thenReturnOneCourse() {
        //arrange
        when(courseRepository.findCourseByCourseId(anyString()))
                .thenReturn(Mono.just(course1));

        //act
        Mono<CourseResponseModel> result = courseService.getCourseByCourseId(course1.getCourseId());

        //assert
        StepVerifier
                .create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(course1.getCourseNumber(), courseResponseModel.courseNumber());
                    return true;
                })
                .verifyComplete();
    }
    @Test
    public void whenAddCourse_thenReturnNewCourse() {
        //arrange
        CourseRequestModel requestModel = new CourseRequestModel("dog-425", "New Course", 30, 2.0, "English");
        Course courseEntity = EntityModelMapper.toEntity(requestModel);
        courseEntity.setId(100);

        when(courseRepository.save(any(Course.class))).thenReturn(Mono.just(courseEntity));

        //act
        Mono<CourseResponseModel> result = courseService.addCourse(Mono.just(requestModel));

        //assert
        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertNotNull(response);
                    assertEquals(requestModel.courseNumber(), response.courseNumber());
                    assertNotNull(response.courseId());
                })
                .verifyComplete();
    }

    @Test
    void whenUpdateCourse_withValidId_thenReturnUpdatedCourse() {
        //arrange
        CourseRequestModel requestModel = new CourseRequestModel("dog-420-updated", "Updated Course", 40, 3.0, "Updated Dept");
        Course updatedCourse = EntityModelMapper.toEntity(requestModel);
        updatedCourse.setId(course1.getId());
        updatedCourse.setCourseId(course1.getCourseId());

        when(courseRepository.findCourseByCourseId(course1.getCourseId())).thenReturn(Mono.just(course1));
        when(courseRepository.save(any(Course.class))).thenReturn(Mono.just(updatedCourse));

        //act
        Mono<CourseResponseModel> result = courseService.updateCourse(Mono.just(requestModel), course1.getCourseId());

        //assert
        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(requestModel.courseName(), response.courseName());
                    assertEquals(requestModel.numHours(), response.numHours());
                    assertEquals(course1.getCourseId(), response.courseId());
                })
                .verifyComplete();
    }



    }
