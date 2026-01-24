package com.roadtech.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * This filter runs once per HTTP request.
 * Its responsibility is to:
 * 1. Read the JWT token from the Authorization header
 * 2. Validate the token
 * 3. Set authentication in Spring Security context if token is valid
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // Service used to extract username and validate JWT token
    private final JwtService jwtService;

    // Loads user details from database using email/username
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Read Authorization header from incoming request
        final String authHeader = request.getHeader("Authorization");

        // If Authorization header is missing or does not start with "Bearer ",
        // skip JWT processing and continue the filter chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token by removing "Bearer " prefix
            final String jwt = authHeader.substring(7);

            // Extract username/email from JWT token
            final String userEmail = jwtService.extractUsername(jwt);

            // Proceed only if username is present and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Validate JWT token against user details
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Create authentication token with user authorities
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Attach request-specific authentication details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Store authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log any exception during JWT processing
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continue with remaining filters
        filterChain.doFilter(request, response);
    }
}
