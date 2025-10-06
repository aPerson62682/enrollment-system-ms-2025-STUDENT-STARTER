package com.champlain.enrollmentsservice.businesslayer.enrollments;

import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnrollmentService {
    Mono<EnrollmentResponseModel> addEnrollment(Mono<EnrollmentRequestModel> enrollmentRequestModel);

    Flux<EnrollmentResponseModel> getAllEnrollments();
    // getbyid
    Mono<EnrollmentResponseModel> getEnrollmentByEnrollmentId(String enrollmentId);

     // update
    Mono<EnrollmentResponseModel> updateEnrollment(Mono<EnrollmentRequestModel> enrollmentRequestModel, String enrollmentId);

    //delete
    Mono<EnrollmentResponseModel> deleteEnrollmentByEnrollmentId (String enrollmentId);

}
