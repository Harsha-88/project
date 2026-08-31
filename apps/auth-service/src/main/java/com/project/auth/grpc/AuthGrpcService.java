package com.project.auth.grpc;

import org.springframework.transaction.annotation.Transactional;
import auth.Auth;
import auth.AuthServiceGrpc;
import com.project.auth.entity.User;
import com.project.auth.repository.UserRepository;
import com.project.auth.session.SessionService;
import com.project.auth.outbox.OutboxEvent;
import com.project.auth.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private record AuthEventPayload(
            String eventType,
            String userId,
            String email,
            Instant timestamp,
            String source
    ) {}

    private static final Logger logger =
            LoggerFactory.getLogger(AuthGrpcService.class);

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public AuthGrpcService(
            UserRepository userRepository,
            SessionService sessionService,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void signup(
            Auth.SignupRequest request,
            StreamObserver<Auth.SignupResponse> responseObserver) {

        try {
            // Validate request
            if (request.getEmail().isBlank()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Email is required")
                                .asRuntimeException()
                );
                return;
            }

            if (request.getPassword().isBlank()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Password is required")
                                .asRuntimeException()
                );
                return;
            }

            // Check whether user already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("Email already exists")
                                .asRuntimeException()
                );
                return;
            }

            // Create user
            User user = new User(
                    request.getEmail(),
                    request.getPassword()
            );

            userRepository.save(user);

            // Create outbox event after successful user creation
            try {
                String payload = objectMapper.writeValueAsString(
                        new AuthEventPayload(
                                "USER_SIGNUP_SUCCESS",
                                user.getId(),
                                user.getEmail(),
                                Instant.now(),
                                "auth-service"
                        )
                );

                outboxEventRepository.save(
                        new OutboxEvent(
                                "USER_SIGNUP_SUCCESS",
                                payload
                        )
                );
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Failed to create signup outbox event",
                        e
                );
            }

            // Successful response
            Auth.SignupResponse response =
                    Auth.SignupResponse.newBuilder()
                            .setSuccess(true)
                            .setMessage("Signup successful")
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            // Log the complete unexpected exception and stack trace
            logger.error(
                    "Unexpected error during signup for email: {}",
                    request.getEmail(),
                    e
            );

            // Return INTERNAL to the gRPC client
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(
                                    "An internal error occurred during signup"
                            )
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getSessionEmail(
            Auth.GetSessionEmailRequest request,
            io.grpc.stub.StreamObserver<Auth.GetSessionEmailResponse> responseObserver) {

        String email = sessionService.getSessionEmail(request.getSessionToken());

        if (email == null || email.isBlank()) {
            responseObserver.onError(
                    Status.UNAUTHENTICATED
                            .withDescription("Invalid or expired session")
                            .asRuntimeException()
            );
            return;
        }

        responseObserver.onNext(
                Auth.GetSessionEmailResponse.newBuilder()
                        .setEmail(email)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void login(
            Auth.LoginRequest request,
            StreamObserver<Auth.LoginResponse> responseObserver) {

        try {
            // Validate request
            if (request.getEmail().isBlank()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Email is required")
                                .asRuntimeException()
                );
                return;
            }

            if (request.getPassword().isBlank()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Password is required")
                                .asRuntimeException()
                );
                return;
            }

            // Find user without converting Optional to null
            var userOptional = userRepository.findByEmail(request.getEmail());

            // Invalid credentials
            if (userOptional.isEmpty() ||
                    !userOptional.get().getPassword().equals(request.getPassword())) {

                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Invalid email or password")
                                .asRuntimeException()
                );
                return;
            }

            User user = userOptional.get();

            // Generate session token
            String sessionToken = UUID.randomUUID().toString();

            // Save session
            sessionService.saveSession(
                    sessionToken,
                    request.getEmail()
            );


            // Successful response
            Auth.LoginResponse response =
                    Auth.LoginResponse.newBuilder()
                            .setSuccess(true)
                            .setMessage("Login successful")
                            .setSessionToken(sessionToken)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            // Log the complete unexpected exception and stack trace
            logger.error(
                    "Unexpected error during login for email: {}",
                    request.getEmail(),
                    e
            );

            // Return INTERNAL to the gRPC client
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(
                                    "An internal error occurred during login"
                            )
                            .asRuntimeException()
            );
        }
    }
}