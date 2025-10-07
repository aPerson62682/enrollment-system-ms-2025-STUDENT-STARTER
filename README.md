# Enrollment System — Microservices Project (2025)
This Enrollment System was a school project focused on exploring microservices architecture using Java 17, Spring Boot 3, and Docker Compose.
 The project demonstrates how distributed services can communicate through RESTful APIs while managing different parts of an educational platform such as students, courses, and enrollments.

## Overview
The system was provided as an incomplete starter project for a microservices course and was completed to demonstrate real-world backend design principles.
It includes three Spring Boot microservices:

- Students Service: Manages student information and profiles
- Courses Service: Handles course listings and details
- Enrollments Service: Connects students with courses and manages enrollments


Each service runs independently and communicates via REST APIs. The project can be run locally or containerized using Docker Compose.

## Project Structure

```
enrollment-system-ms-2025-STUDENT-STARTER/
├── students-service/        # Handles student-related operations
├── courses-service/         # Handles course-related operations
├── enrollments-service/     # Manages course enrollment logic
├── data/                    # Data initialization and utility scripts
├── docker-compose.yml       # Docker configuration for all microservices
├── gradlew / gradlew.bat    # Gradle wrapper scripts
└── create-projects.bash     # Script to set up submodules
```


## Tech Stack

| Category | Technology |
|-----------|-------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Build Tool** | Gradle |
| **Database** | H2 / PostgreSQL |
| **Architecture** | REST Microservices |
| **Containerization** | Docker & Docker Compose |
| **Testing** | JUnit 5, Mockito |



## Running the Project
To start all microservices together using Docker:

```docker-compose up --build```

Each service runs on its own port:

Students → ```http://localhost:7001```


Courses → ```http://localhost:7002```


Enrollments → ```http://localhost:7003```


To stop everything:

```docker-compose down```


## Example API Endpoints

| Service | Method | Endpoint | Description |
|----------|---------|-----------|--------------|
| **Students** | GET | `/api/v1/students` | Get all students |
| **Students** | POST | `/api/v1/students` | Add a new student |
| **Courses** | GET | `/api/v1/courses` | Get all courses |
| **Enrollments** | POST | `/api/v1/enrollments` | Enroll a student in a course |
| **Enrollments** | GET | `/api/v1/enrollments/{studentId}` | Get a student’s enrolled courses |


##  Learning Objectives
Through this project, I learned how to:
Set up and run multiple Spring Boot microservices


Use Docker Compose for multi-service environments


Understand REST API communication between services


Explore layered architecture (Controller → Service → Repository)


Perform basic integration and unit testing




