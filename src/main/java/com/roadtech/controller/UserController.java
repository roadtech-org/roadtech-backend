package com.roadtech.controller;

import com.roadtech.dto.UserDto;
import com.roadtech.entity.User;
import com.roadtech.exception.ResourceNotFoundException;
import com.roadtech.repository.UserRepository;
import com.roadtech.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for handling
 * user profile related APIs.
 */
@RestController
@RequestMapping("/users") // Base URL for all user-related endpoints
@RequiredArgsConstructor // Lombok: generates constructor for final fields
@Tag(name = "Users", description = "User profile endpoints") // Swagger grouping
@SecurityRequirement(name = "bearerAuth") // Requires JWT authentication
public class UserController {

    // Repository used to fetch and persist User entities
    private final UserRepository userRepository;

    /**
     * Fetches the profile of the currently authenticated user.
     * Endpoint: GET /users/me
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getCurrentUser(
            // Injects authenticated user details from Spring Security context
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Fetch user from database using ID from JWT
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() ->
                        // Thrown if user does not exist in database
                        new ResourceNotFoundException("User", userDetails.getUserId())
                );

        // Convert entity to DTO and return response
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    /**
     * Updates profile details of the currently authenticated user.
     * Endpoint: PUT /users/me
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDto> updateCurrentUser(
            // Authenticated user info extracted from JWT
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // Request body containing fields to be updated
            @RequestBody UserDto updateDto
    ) {
        // Fetch existing user from database
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() ->
                        // Thrown if user is not found
                        new ResourceNotFoundException("User", userDetails.getUserId())
                );

        // Update full name only if provided in request
        if (updateDto.getFullName() != null) {
            user.setFullName(updateDto.getFullName());
        }

        // Update phone number only if provided in request
        if (updateDto.getPhone() != null) {
            user.setPhone(updateDto.getPhone());
        }

        // Persist updated user entity
        user = userRepository.save(user);

        // Return updated user profile as DTO
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }
}
