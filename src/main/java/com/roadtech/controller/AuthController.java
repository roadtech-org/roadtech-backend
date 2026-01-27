package com.roadtech.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * This controller handles all authentication-related APIs
 * like register, login, refresh token, and logout.
 * 
 * It only receives HTTP requests and sends responses.
 * All business logic is delegated to AuthService.
 */

import com.roadtech.dto.auth.AuthResponse;
import com.roadtech.dto.auth.LoginRequest;
import com.roadtech.dto.auth.RefreshTokenRequest;
import com.roadtech.dto.auth.RegisterRequest;
import com.roadtech.service.AuthService;

/*
 * Swagger annotations are used to generate API documentation
 * so that APIs can be tested and understood using Swagger UI.
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/*
 * @Valid is used to trigger validation on request body
 * based on validation annotations present in DTO classes.
 */
import jakarta.validation.Valid;
/*
 * Lombok annotation that generates constructor
 * for all final fields automatically.
 */
import lombok.RequiredArgsConstructor;

@RestController
/*
 * Marks this class as a REST controller.
 * It automatically converts Java objects to JSON responses.
 */

@RequestMapping("/auth")
/*
 * Base URL for all authentication APIs.
 * Example:
 * /auth/register
 * /auth/login
 */

@RequiredArgsConstructor
/*
 * Injects AuthService using constructor injection.
 * We don't need to write constructor manually.
 */

@Tag(name = "Authentication", description = "Authentication endpoints")
/*
 * Groups all auth-related APIs in Swagger UI
 * under "Authentication" section.
 */
public class AuthController {

    /*
     * AuthService contains the actual authentication logic
     * like saving user, validating password, generating JWT tokens.
     */
    private final AuthService authService;

    @PostMapping("/register")
    /*
     * Handles HTTP POST request for user registration.
     * Endpoint: POST /auth/register
     */
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        /*
         * @RequestBody converts incoming JSON into RegisterRequest object.
         * @Valid checks input validations (email format, password length, etc.).
         */

        AuthResponse response = authService.register(request);
        /*
         * Calls service layer to register user.
         * Service handles password hashing, saving user, and token generation.
         */

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        /*
         * Returns HTTP 201 (Created) with authentication response.
         */
    }

    @PostMapping("/login")
    /*
     * Handles HTTP POST request for user login.
     * Endpoint: POST /auth/login
     */
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        /*
         * LoginRequest contains email and password.
         * Validation ensures correct input format.
         */
        AuthResponse response = authService.login(request);
        /*
         * Service verifies credentials and generates access & refresh tokens.
         */

        return ResponseEntity.ok(response);
        /*
         * Returns HTTP 200 (OK) with tokens and user info.
         */
    }

    @PostMapping("/refresh")
    /*
     * Handles request to generate new access token
     * using a valid refresh token.
     * Endpoint: POST /auth/refresh
     */
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        /*
         * RefreshTokenRequest contains only refresh token.
         */
        AuthResponse response =
                authService.refreshToken(request.getRefreshToken());
        /*
         * Service validates refresh token and issues new access token.
         */

        return ResponseEntity.ok(response);
        /*
         * Returns HTTP 200 with new access token.
         */
    }

    @PostMapping("/logout")
    /*
     * Handles logout request.
     * Endpoint: POST /auth/logout
     */
    @Operation(summary = "Logout and invalidate refresh token")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        /*
         * Logout works by invalidating refresh token
         * so it cannot be used again.
         */
        authService.logout(request.getRefreshToken());

        return ResponseEntity.noContent().build();
        /*
         * Returns HTTP 204 (No Content) indicating successful logout.
         */
    }
}
