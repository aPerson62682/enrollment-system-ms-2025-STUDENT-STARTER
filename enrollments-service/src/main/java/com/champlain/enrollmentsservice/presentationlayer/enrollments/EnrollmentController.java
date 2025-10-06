package com.champlain.enrollmentsservice.presentationlayer.enrollments;

import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentService;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.domainclientlayer.students.StudentResponseModel;
import com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions;
import com.champlain.enrollmentsservice.validation.RequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentRepository enrollmentRepository;


    public EnrollmentController(EnrollmentService enrollmentService, EnrollmentRepository enrollmentRepository) {
        this.enrollmentService = enrollmentService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EnrollmentResponseModel> getAllEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    @GetMapping("{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponseModel>> getEnrollmentByEnrollmentId(@PathVariable String enrollmentId) {
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions.invalidEnrollmentId(enrollmentId))
                .flatMap(enrollmentService::getEnrollmentByEnrollmentId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions.enrollmentNotFound(enrollmentId));
    }

    @PostMapping()
    public Mono<ResponseEntity<EnrollmentResponseModel>> addEnrollment(@RequestBody Mono<EnrollmentRequestModel> enrollmentRequestModel) {
        return enrollmentRequestModel
                .as(enrollmentService::addEnrollment)
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e));
    }

    @PutMapping("{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponseModel>> updateEnrollment(@RequestBody Mono<EnrollmentRequestModel> enrollmentRequestModel, @PathVariable String enrollmentId) {
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidEnrollmentId(enrollmentId))
                .thenReturn(enrollmentRequestModel.transform(RequestValidator.validateBody()))
                .flatMap(validReq -> enrollmentService.updateEnrollment(validReq, enrollmentId))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.enrollmentNotFound(enrollmentId));
    }

    @DeleteMapping("{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponseModel>> deleteEnrollment (@PathVariable String enrollmentId){
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions.invalidEnrollmentId(enrollmentId))
                .flatMap(enrollmentService::deleteEnrollmentByEnrollmentId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions.enrollmentNotFound(enrollmentId));
    }


}
