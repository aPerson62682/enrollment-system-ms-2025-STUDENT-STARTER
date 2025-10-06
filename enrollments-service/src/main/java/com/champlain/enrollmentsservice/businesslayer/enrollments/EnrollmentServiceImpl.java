package com.champlain.enrollmentsservice.businesslayer.enrollments;

import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.domainclientlayer.courses.CourseServiceClient;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentServiceClientAsynchronous;
import com.champlain.enrollmentsservice.mapper.EntityModelMapper;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentServiceClientAsynchronous studentClient;
    private final CourseServiceClient courseClient;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, StudentServiceClientAsynchronous studentClient, CourseServiceClient courseClient) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentClient = studentClient;
        this.courseClient = courseClient;
    }


    @Override
    public Mono<EnrollmentResponseModel> addEnrollment(Mono<EnrollmentRequestModel> enrollmentRequestModel) {
        return enrollmentRequestModel
                .map(RequestContext::new)
                .flatMap(this::studentRequestResponse)
                .flatMap(this::courseRequestResponse)
                .map(EntityModelMapper::toEntity)
                .flatMap(enrollmentRepository::save)
                .map(EntityModelMapper::toModel);
    }

    @Override
    public Flux<EnrollmentResponseModel> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .map(EntityModelMapper::toModel);
    }


    @Override
    public Mono<EnrollmentResponseModel> updateEnrollment(Mono<EnrollmentRequestModel> enrollmentRequestModel, String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .flatMap(found -> enrollmentRequestModel
                     .map(RequestContext::new)
                     .flatMap(this::studentRequestResponse)
                     .flatMap(this::courseRequestResponse)
                        .map(EntityModelMapper::toEntity)
                        .doOnNext(e -> e.setEnrollmentId(enrollmentId))
                        .doOnNext(e -> e.setId(found.getId())))
                        .flatMap(enrollmentRepository::save)
                        .map(EntityModelMapper::toModel);
    }

    @Override
    public Mono<EnrollmentResponseModel> deleteEnrollmentByEnrollmentId(String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .flatMap(found -> enrollmentRepository.delete(found)
                .then(Mono.just(found))) //create a new mono using the found entity
                .map(EntityModelMapper::toModel);    }


    @Override
    public Mono<EnrollmentResponseModel> getEnrollmentByEnrollmentId(String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .doOnNext(c -> log.debug("Enrollment found has id: {}", c.getEnrollmentId()))
                .map(EntityModelMapper::toModel);
    }



    private Mono<RequestContext> studentRequestResponse(RequestContext rc){
        return studentClient.getStudentByStudentId(rc.getEnrollmentRequestModel().studentId())
                .doOnNext(rc::setStudentResponseModel)
                .thenReturn(rc);
    }

    private Mono<RequestContext> courseRequestResponse(RequestContext rc) {
        return courseClient.getCourseByCourseId(rc.getEnrollmentRequestModel().courseId())
                .doOnNext(rc::setCourseResponseModel)
                .thenReturn(rc);
    }
}
