package com.project.auth.grpc;

import auth.Auth;
import auth.AuthServiceGrpc;
import com.project.auth.entity.User;
import com.project.auth.repository.UserRepository;
import com.project.auth.session.SessionService;
import com.project.auth.kafka.AuthEventProducer;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

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
            if (userRepository.existsByEmail(request.getEmail())) {
                Auth.SignupResponse response = Auth.SignupResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Email already exists")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            User user = new User(
                    request.getEmail(),
                    request.getPassword()
            );

            userRepository.save(user);

            authEventProducer.publish(
                    "SIGNUP_SUCCESS:" + user.getEmail()
            );

            Auth.SignupResponse response = Auth.SignupResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Signup successful")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            Auth.SignupResponse response = Auth.SignupResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Signup failed")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void login(
            Auth.LoginRequest request,
            StreamObserver<Auth.LoginResponse> responseObserver) {

        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);

            if (user == null || !user.getPassword().equals(request.getPassword())) {
                Auth.LoginResponse response = Auth.LoginResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Invalid email or password")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            String sessionToken = UUID.randomUUID().toString();

            sessionService.saveSession(sessionToken, request.getEmail());

            authEventProducer.publish(
                    "LOGIN_SUCCESS:" + user.getEmail()
            );

            Auth.LoginResponse response = Auth.LoginResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Login successful")
                    .setSessionToken(sessionToken)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            Auth.LoginResponse response = Auth.LoginResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Login failed")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
