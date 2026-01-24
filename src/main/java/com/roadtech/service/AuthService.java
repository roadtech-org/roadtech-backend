package com.roadtech.service;

import com.roadtech.dto.UserDto;
import com.roadtech.dto.auth.AuthResponse;
import com.roadtech.dto.auth.LoginRequest;
import com.roadtech.dto.auth.RegisterRequest;
import com.roadtech.entity.MechanicProfile;
import com.roadtech.entity.RefreshToken;
import com.roadtech.entity.User;
import com.roadtech.entity.User.UserRole;
import com.roadtech.exception.BadRequestException;
import com.roadtech.exception.UnauthorizedException;
import com.roadtech.repository.MechanicProfileRepository;
import com.roadtech.repository.RefreshTokenRepository;
import com.roadtech.repository.UserRepository;
import com.roadtech.security.CustomUserDetails;
import com.roadtech.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service // Marks this class as a Spring service component
@RequiredArgsConstructor // Lombok: generates constructor for all final fields
@Slf4j // Lombok: provides a logger instance
public class AuthService {

    // Repository for user-related database operations
    private final UserRepository userRepository;

    // Repository for mechanic profile operations
    private final MechanicProfileRepository mechanicProfileRepository;

    // Repository for refresh token storage and lookup
    private final RefreshTokenRepository refreshTokenRepository;

    // Used to hash passwords securely
    private final PasswordEncoder passwordEncoder;

    // Handles JWT creation and expiration logic
    private final JwtService jwtService;

    // Spring Security component for authentication
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user (customer or mechanic)
     * Runs in a transaction to ensure atomicity
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Build and save the user entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encrypt password
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // If the user is a mechanic, create a mechanic profile
        if (request.getRole() == UserRole.MECHANIC) {
            MechanicProfile profile = MechanicProfile.builder()
                    .user(user)
                    .specializations(request.getSpecializations())
                    .isAvailable(false) // Default availability
                    .build();
            mechanicProfileRepository.save(profile);
        }

        // Generate access & refresh tokens and return response
        return generateAuthResponse(user);
    }

    /**
     * Authenticates a user and returns JWT tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Authenticate using Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Fetch active user from database
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Generate tokens and return response
        return generateAuthResponse(user);
    }

    /**
     * Generates a new access token using a valid refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {

        // Fetch refresh token from database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        // Check if refresh token is expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        // Remove old refresh token (token rotation)
        refreshTokenRepository.delete(refreshToken);

        // Generate new tokens
        return generateAuthResponse(user);
    }

    /**
     * Logs out the user by deleting the refresh token
     */
    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Creates access token, refresh token, and user DTO
     */
    private AuthResponse generateAuthResponse(User user) {

        // Convert User entity to Spring Security UserDetails
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Generate JWT access token
        String accessToken = jwtService.generateAccessToken(userDetails);

        // Generate and store refresh token
        String refreshTokenStr = generateRefreshToken(user);

        // Build authentication response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(UserDto.fromEntity(user))
                .build();
    }

    /**
     * Generates and stores a refresh token in the database
     */
    private String generateRefreshToken(User user) {

        // Create random UUID for refresh token
        String tokenValue = UUID.randomUUID().toString();

        // Build refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(
                        LocalDateTime.now().plusSeconds(
                                jwtService.getRefreshTokenExpiration() / 1000
                        )
                )
                .build();

        // Persist refresh token
        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }
}
