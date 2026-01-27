package com.roadtech.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roadtech.dto.UserDto;
import com.roadtech.dto.auth.AuthResponse;
import com.roadtech.dto.auth.LoginRequest;
import com.roadtech.dto.auth.RegisterRequest;
import com.roadtech.entity.MechanicProfile;
import com.roadtech.entity.PartsProvider;
import com.roadtech.entity.RefreshToken;
import com.roadtech.entity.User;
import com.roadtech.entity.User.UserRole;
import com.roadtech.exception.BadRequestException;
import com.roadtech.exception.UnauthorizedException;
import com.roadtech.repository.MechanicProfileRepository;
import com.roadtech.repository.PartsProviderRepository;
import com.roadtech.repository.RefreshTokenRepository;
import com.roadtech.repository.UserRepository;
import com.roadtech.security.CustomUserDetails;
import com.roadtech.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final MechanicProfileRepository mechanicProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PartsProviderRepository partsProviderRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Create mechanic profile if registering as mechanic
        if (request.getRole() == UserRole.MECHANIC) {
            MechanicProfile profile = MechanicProfile.builder()
                    .user(user)
                    .specializations(request.getSpecializations())
                    .isAvailable(false)
                    .build();
            mechanicProfileRepository.save(profile);
        }

        // // ✅ PARTS PROVIDER PROFILE
        if (request.getRole() == UserRole.PARTS_PROVIDER) {

            if (request.getShopName() == null || request.getAddress() == null) {
                throw new BadRequestException("Shop details are required");
            }

            PartsProvider provider = PartsProvider.builder()
                    .user(user)
                    .shopName(request.getShopName())
                    .address(request.getAddress())
                    .latitude(BigDecimal.valueOf(request.getLatitude()))
                    .longitude(BigDecimal.valueOf(request.getLongitude()))
                    .phone(request.getPhone())
                    .openingTime(request.getOpeningTime())
                    .closingTime(request.getClosingTime())
                    .isOpen(false)
                    .isVerified(false)
                    .build();

            partsProviderRepository.save(provider);
        }
        
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        // Delete old refresh token
        refreshTokenRepository.delete(refreshToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(refreshTokenRepository::delete);
    }

    private AuthResponse generateAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshTokenStr = generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .user(UserDto.fromEntity(user))
                .build();
    }

    private String generateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}
