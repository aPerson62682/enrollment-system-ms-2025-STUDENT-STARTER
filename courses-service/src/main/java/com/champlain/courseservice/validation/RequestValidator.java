package com.champlain.courseservice.validation;

import com.champlain.courseservice.exceptionhandling.ApplicationExceptions;
import com.champlain.courseservice.presentationlayer.CourseRequestModel;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Slf4j
public class RequestValidator {
    public static UnaryOperator<Mono<CourseRequestModel>> validateBody(){
        return courseRequestModel -> courseRequestModel
                .filter(hasCourseNumber())
                .switchIfEmpty(ApplicationExceptions.missingCourseNumber())
                .filter(hasCourseName())
                .switchIfEmpty(ApplicationExceptions.missingCourseName())
                .filter(hasValidCredits())
                .switchIfEmpty(ApplicationExceptions.invalidCourseCredits())
                .filter(hasValidHours())
                .switchIfEmpty(ApplicationExceptions.invalidCourseHours());


    }

    private static Predicate<CourseRequestModel> hasCourseNumber(){
        return courseRequestModel -> Objects.nonNull(courseRequestModel.courseNumber());
    }

    private static Predicate<CourseRequestModel> hasCourseName(){
        return courseRequestModel -> Objects.nonNull(courseRequestModel.courseName());
    }

    private static Predicate<CourseRequestModel> hasValidCredits(){
        return courseRequestModel -> Objects.nonNull(courseRequestModel.numCredits())
                && (courseRequestModel.numCredits() > 0);
    }

    private static Predicate<CourseRequestModel> hasValidHours(){
        return courseRequestModel -> Objects.nonNull(courseRequestModel.numHours())
                && (courseRequestModel.numHours() > 0);
    }


}
