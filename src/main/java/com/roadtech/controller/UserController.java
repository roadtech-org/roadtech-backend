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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", userDetails.getUserId()));
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDto> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserDto updateDto
    ) {
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", userDetails.getUserId()));

        if (updateDto.getFullName() != null) {
            user.setFullName(updateDto.getFullName());
        }
        if (updateDto.getPhone() != null) {
            user.setPhone(updateDto.getPhone());
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }
}
