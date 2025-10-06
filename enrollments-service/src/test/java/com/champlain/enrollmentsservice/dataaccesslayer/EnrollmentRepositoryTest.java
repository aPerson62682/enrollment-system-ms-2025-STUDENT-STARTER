package com.champlain.enrollmentsservice.dataaccesslayer;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles
class EnrollmentRepositoryTest {

      @Autowired
        private EnrollmentRepository enrollmentRepository;

        @Test
        public void shouldSaveSingleEnrollment() {
            Publisher<Enrollment> setup = enrollmentRepository.deleteAll().thenMany(enrollmentRepository.save(buildEnrollment()));
            StepVerifier
                    .create(setup)
                    .expectNextCount(1)
                    .verifyComplete();
        }

        private Enrollment buildEnrollment() {
            return Enrollment.builder()
                    .enrollmentId(UUID.randomUUID().toString())
                    .id("20")
                    .enrollmentYear(2005)
                    .courseName("Web Services")
                    .studentId(UUID.randomUUID().toString())
                    .semester(Semester.FALL)
                    .studentFirstName("Christopher")
                    .studentLastName("Hernandez-Dauplo")
                    .courseId(UUID.randomUUID().toString())
                    .courseNumber("Web-200")
                    .build();
        }

}
