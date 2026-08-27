package com.project.auth.grpc;

import auth.Auth;
import auth.AuthServiceGrpc;
import com.project.auth.entity.User;
import com.project.auth.repository.UserRepository;
import com.project.auth.session.SessionService;
import com.project.auth.kafka.AuthEventProducer;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthGrpcService.class);

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final AuthEventProducer authEventProducer;

    public AuthGrpcService(
            UserRepository userRepository,
            AuthEventProducer authEventProducer,
            SessionService sessionService) {

        this.userRepository = userRepository;
        this.authEventProducer = authEventProducer;
        this.sessionService = sessionService;
    }

    @Override
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

            // Publish signup event
            authEventProducer.publish(
                    "SIGNUP_SUCCESS:" + user.getEmail()
            );

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

            // Find user
            User user = userRepository
                    .findByEmail(request.getEmail())
                    .orElse(null);

            // Invalid credentials
            if (user == null ||
                    !user.getPassword().equals(request.getPassword())) {

                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Invalid email or password")
                                .asRuntimeException()
                );
                return;
            }

            // Generate session token
            String sessionToken = UUID.randomUUID().toString();

            // Save session
            sessionService.saveSession(
                    sessionToken,
                    request.getEmail()
            );

            // Publish login event
            authEventProducer.publish(
                    "LOGIN_SUCCESS:" + user.getEmail()
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