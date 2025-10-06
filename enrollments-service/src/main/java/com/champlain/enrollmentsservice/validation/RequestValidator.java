package com.champlain.enrollmentsservice.validation;

import com.champlain.enrollmentsservice.exceptionhandling.ApplicationExceptions;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Year;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;


@Slf4j
public class RequestValidator {

    public static UnaryOperator<Mono<EnrollmentRequestModel>> validateBody() {
        return enrollmentRequestModel -> enrollmentRequestModel
                .filter(hasStudentId())
                .switchIfEmpty(ApplicationExceptions.missingStudentId())
                .filter(hasCourseId())
                .switchIfEmpty(ApplicationExceptions.missingCourseId())
                .filter(hasSemester())
                .switchIfEmpty(ApplicationExceptions.missingSemester())
                .filter(hasValidEnrollmentYear())
                .switchIfEmpty(ApplicationExceptions.invalidEnrollmentYear());
    }

    private static Predicate<EnrollmentRequestModel> hasStudentId() {
        return enrollmentRequestModel ->
                Objects.nonNull(enrollmentRequestModel.studentId()) &&
                        !enrollmentRequestModel.studentId().isBlank();
    }

    private static Predicate<EnrollmentRequestModel> hasCourseId() {
        return enrollmentRequestModel ->
                Objects.nonNull(enrollmentRequestModel.courseId()) &&
                        !enrollmentRequestModel.courseId().isBlank();
    }

    private static Predicate<EnrollmentRequestModel> hasSemester() {
        return enrollmentRequestModel ->
                Objects.nonNull(enrollmentRequestModel.semester());
    }

    private static Predicate<EnrollmentRequestModel> hasValidEnrollmentYear() {
        return enrollmentRequestModel -> {
            Integer year = enrollmentRequestModel.enrollmentYear();
            if (year == null) return false;
            int current = Year.now().getValue();
            return year >= 2000 && year <= current + 1;
        };
    }
}


