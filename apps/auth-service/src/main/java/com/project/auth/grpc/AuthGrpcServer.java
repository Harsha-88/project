package com.project.auth.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;

@Component
public class AuthGrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(AuthGrpcServer.class);

    private Server server;

    private final AuthGrpcService authGrpcService;

    public AuthGrpcServer(AuthGrpcService authGrpcService) {
        this.authGrpcService = authGrpcService;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder
                .forPort(50051)
                .addService(authGrpcService)
                .build()
                .start();

        logger.info("gRPC server started on port 50051");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AuthGrpcServer.this.stop();
        }));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
