package com.champlain.enrollmentsservice.presentationlayer.enrollments;

import org.mockito.Mock;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockServerConfig {

    /// Start a MockServer instance and make it available as a ClientServer bean

    @Bean
    public ClientAndServer mockServer() {
        return ClientAndServer.startClientAndServer(1080); // or any desired port
    }
    // Create MockServerClient using the information(port) from ClientAndServer bean
    @Bean
    public MockServerClient mockServerClient(ClientAndServer mockServer) {
        return new MockServerClient("localhost", mockServer.getPort());
    }
}
