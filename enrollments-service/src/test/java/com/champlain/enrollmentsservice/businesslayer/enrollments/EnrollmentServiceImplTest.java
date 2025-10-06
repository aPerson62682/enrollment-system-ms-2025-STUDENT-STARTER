package com.champlain.enrollmentsservice.businesslayer.enrollments;

import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseResponseModel;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseServiceClient;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentResponseModel;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentServiceClientAsynchronous;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.CourseNotFoundException;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.InvalidCourseIdException;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.InvalidStudentIdException;
import com.champlain.enrollmentsservice.exceptionhandling.exceptions.StudentNotFoundException;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static com.champlain.enrollmentsservice.dataaccesslayer.Semester.FALL;
import static com.champlain.enrollmentsservice.dataaccesslayer.Semester.WINTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private StudentServiceClientAsynchronous studentServiceClientAsynchronous;
    @Mock
    private CourseServiceClient courseServiceClient;

    private final String STUDENT_ID = UUID.randomUUID().toString();
    private final String COURSE_ID = UUID.randomUUID().toString();
    private final String ENROLLMENT_ID = UUID.randomUUID().toString();


    Enrollment enrollment1 = Enrollment.builder()
            .id("1")
            .enrollmentId(ENROLLMENT_ID)
            .enrollmentYear(2006)
            .semester(FALL)
            .studentId(STUDENT_ID)
            .studentFirstName("Christopher")
            .studentLastName("Hernandez-Dauplo")
            .courseId(COURSE_ID)
            .courseNumber("WEB-200")
            .courseName("Web Services")
            .build();


    Enrollment enrollment2 = Enrollment.builder()
            .id("2")
            .enrollmentId(UUID.randomUUID().toString())
            .enrollmentYear(2006)
            .semester(FALL)
            .studentId(UUID.randomUUID().toString())
            .studentFirstName("Daanyal")
            .studentLastName("Khan")
            .courseId(UUID.randomUUID().toString())
            .courseNumber("WEB-420")
            .courseName("Web Services")
            .build();

    Enrollment enrollment3 = Enrollment.builder()
            .id("3")
            .enrollmentId(UUID.randomUUID().toString())
            .enrollmentYear(2006)
            .semester(WINTER)
            .studentId(UUID.randomUUID().toString())
            .studentFirstName("Faizaan")
            .studentLastName("Khalid")
            .courseId(UUID.randomUUID().toString())
            .courseNumber("WEB-067")
            .courseName("Web Services")
            .build();

    StudentResponseModel student = new StudentResponseModel(
            STUDENT_ID,
            "Jeremy",
            "Misola-Rellin",
            "Computer Science",
            "Stuff"
    );
    CourseResponseModel course = new CourseResponseModel(
            COURSE_ID,
            "ENG-690",
            "English",
            10,
            10.5,
            "Eng"
    );

    String NON_EXISTING_UUID = "61b546c1-7ebb-48d3-aeaa-1015509ae190";
    String INVALID_UUID = "This-Is-An-Invalid-UUID-Format";


    @Test
    public void whenGetAllEnrollments_thenReturnAllEnrollments(){
        //arrange
        when(enrollmentRepository.findAll())
                .thenReturn(Flux.just(enrollment1, enrollment2, enrollment3));

        //act
        Flux<EnrollmentResponseModel> result = enrollmentService.getAllEnrollments();

        //assert
        StepVerifier
                .create(result)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void whenGetEnrollmentByValidId_thenReturnEnrollment() {
        // arrange
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString())).thenReturn(Mono.just(enrollment1));

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.getEnrollmentByEnrollmentId(ENROLLMENT_ID);

        // assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.enrollmentId().equals(ENROLLMENT_ID))
                .verifyComplete();
    }


    @Test
    public void whenUpdateEnrollment_thenReturnUpdatedEnrollment(){
        //arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(
                2010,
                FALL,
                STUDENT_ID,
                COURSE_ID
        );

        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString()))
                .thenReturn(Mono.just(enrollment1));
        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString()))
                .thenReturn(Mono.just(student));
        when(courseServiceClient.getCourseByCourseId(anyString()))
                .thenReturn(Mono.just(course));
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));


        //act
        Mono<EnrollmentResponseModel> updatedEnrollment = enrollmentService.updateEnrollment(Mono.just(requestModel), enrollment1.getEnrollmentId());

        //assert
        StepVerifier
                .create(updatedEnrollment)
                .expectNextMatches(enrollmentResponseModel ->
                        enrollmentResponseModel.enrollmentYear().equals(2010) &&
                                enrollmentResponseModel.semester().equals(FALL) &&
                                enrollmentResponseModel.courseName().equals("English")
                )
                .verifyComplete();
    }

    @Test
    public void whenUpdateEnrollment_withNonExistentEnrollmentId_thenReturnEmptyMono() {
        // arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(2010, FALL, STUDENT_ID, COURSE_ID);
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString())).thenReturn(Mono.empty());

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.updateEnrollment(Mono.just(requestModel), NON_EXISTING_UUID);

        // assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenUpdateEnrollment_withNonExistentStudent_thenThrowStudentNotFoundException() {
        // arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(2010, FALL, NON_EXISTING_UUID, COURSE_ID);
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString())).thenReturn(Mono.just(enrollment1));
        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString())).thenReturn(Mono.error(new StudentNotFoundException("Student with id=" + NON_EXISTING_UUID + " is not found")));

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.updateEnrollment(Mono.just(requestModel), enrollment1.getEnrollmentId());

        // assert
        StepVerifier.create(result)
                .expectError(StudentNotFoundException.class)
                .verify();
    }

    @Test
    public void whenUpdateEnrollment_withNonExistentCourse_thenThrowCourseNotFoundException() {
        // arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(2010, FALL, STUDENT_ID, NON_EXISTING_UUID);
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString())).thenReturn(Mono.just(enrollment1));
        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString())).thenReturn(Mono.just(student));
        when(courseServiceClient.getCourseByCourseId(anyString())).thenReturn(Mono.error(new CourseNotFoundException("Course with id=" + NON_EXISTING_UUID + " is not found")));

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.updateEnrollment(Mono.just(requestModel), enrollment1.getEnrollmentId());

        // assert
        StepVerifier.create(result)
                .expectError(CourseNotFoundException.class)
                .verify();
    }


    @Test
    public void whenAddEnrollment_thenReturnAddedEnrollment(){
        //arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(
                2010,
                FALL,
                STUDENT_ID,
                COURSE_ID
        );

        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString()))
                .thenReturn(Mono.just(student));
        when(courseServiceClient.getCourseByCourseId(anyString()))
                .thenReturn(Mono.just(course));
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));


        //act
        Mono<EnrollmentResponseModel> addedEnrollment = enrollmentService.addEnrollment(Mono.just(requestModel));

        //assert
        StepVerifier
                .create(addedEnrollment)
                .expectNextMatches(enrollmentResponseModel ->
                        enrollmentResponseModel.enrollmentYear().equals(2010) &&
                                enrollmentResponseModel.semester().equals(FALL) &&
                                enrollmentResponseModel.courseName().equals("English")
                )
                .verifyComplete();
    }

    @Test
    public void whenAddEnrollment_withNonExistentStudent_thenThrowStudentNotFoundException() {
        // arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(2010, FALL, NON_EXISTING_UUID, COURSE_ID);
        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString())).thenReturn(Mono.error(new StudentNotFoundException("Student with id=" + NON_EXISTING_UUID + " is not found")));

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.addEnrollment(Mono.just(requestModel));

        // assert
        StepVerifier.create(result)
                .expectError(StudentNotFoundException.class)
                .verify();
    }

    @Test
    public void whenAddEnrollment_withNonExistentCourse_thenThrowCourseNotFoundException() {
        // arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(2010, FALL, STUDENT_ID, NON_EXISTING_UUID);
        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString())).thenReturn(Mono.just(student));
        when(courseServiceClient.getCourseByCourseId(anyString())).thenReturn(Mono.error(new CourseNotFoundException("Course with id=" + NON_EXISTING_UUID + " is not found")));

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.addEnrollment(Mono.just(requestModel));

        // assert
        StepVerifier.create(result)
                .expectError(CourseNotFoundException.class)
                .verify();
    }

    @Test
    public void whenAddEnrollment_withInvalidStudentId_thenThrowInvalidStudentIdException() {
        // arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(2010, FALL, INVALID_UUID, COURSE_ID);
        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString()))
                .thenReturn(Mono.error(new InvalidStudentIdException(INVALID_UUID)));

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.addEnrollment(Mono.just(requestModel));

        // assert
        StepVerifier.create(result)
                .expectError(InvalidStudentIdException.class)
                .verify();
    }

    @Test
    public void whenAddEnrollment_withInvalidCourseId_thenThrowInvalidCourseIdException() {
        // arrange
        EnrollmentRequestModel requestModel = new EnrollmentRequestModel(2010, FALL, STUDENT_ID, INVALID_UUID);
        when(studentServiceClientAsynchronous.getStudentByStudentId(anyString()))
                .thenReturn(Mono.just(student));
        when(courseServiceClient.getCourseByCourseId(anyString()))
                .thenReturn(Mono.error(new InvalidCourseIdException(INVALID_UUID)));

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.addEnrollment(Mono.just(requestModel));

        // assert
        StepVerifier.create(result)
                .expectError(InvalidCourseIdException.class)
                .verify();
    }


    @Test
    public void whenGetEnrollmentByNonExistingUUID_thenReturnEmptyMono(){
        //arrange
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString()))
                .thenReturn(Mono.empty());

        //act
        Mono<EnrollmentResponseModel> enrollment = enrollmentService.getEnrollmentByEnrollmentId(NON_EXISTING_UUID);

        //assert
        StepVerifier
                .create(enrollment)
                .expectComplete()
                .verify();
    }
    @Test
    public void whenDeleteEnrollmentByID_thenReturnDeletedEnrollment(){
        //arrange
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString()))
                .thenReturn(Mono.just(enrollment1));
        when(enrollmentRepository.delete(any()))
                .thenReturn(Mono.empty());

        //act
        Mono<EnrollmentResponseModel> enrollment = enrollmentService.deleteEnrollmentByEnrollmentId(enrollment1.getEnrollmentId());

        //assert
        StepVerifier
                .create(enrollment)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void whenDeleteEnrollment_withNonExistentId_thenReturnEmptyMono() {
        // arrange
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString())).thenReturn(Mono.empty());

        // act
        Mono<EnrollmentResponseModel> result = enrollmentService.deleteEnrollmentByEnrollmentId(NON_EXISTING_UUID);

        // assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }
}

