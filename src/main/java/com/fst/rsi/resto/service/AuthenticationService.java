package com.fst.rsi.resto.service;

import com.fst.rsi.resto.dto.AuthResponseDTO;
import com.fst.rsi.resto.dto.ClientRequestDTO;
import com.fst.rsi.resto.dto.LoginRequestDTO;
import com.fst.rsi.resto.dto.RefreshTokenRequestDTO;
import com.fst.rsi.resto.entity.User;
import com.fst.rsi.resto.exception.BusinessException;
import com.fst.rsi.resto.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepo userRepository;
    private final ClientService clientService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

            user.updateLastLogin();
            user.resetFailedAttempts();
            userRepository.save(user);

            String token = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetailsService.loadUserByUsername(user.getEmail()));

            return AuthResponseDTO.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .type("Bearer")
                    .id(user.getId())
                    .email(user.getEmail())
                    .nom(user.getNom())
                    .prenom(user.getPrenom())
                    .fullName(user.getFullName())
                    .roles(user.getRoles())
                    .build();

        } catch (BadCredentialsException e) {
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElse(null);
            if (user != null) {
                user.incrementFailedAttempts();
                userRepository.save(user);
            }
            throw new BusinessException("Email ou mot de passe incorrect");
        }
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();

            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BusinessException("Refresh token invalide ou expiré");
            }

            String email = jwtTokenProvider.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            String newToken = jwtTokenProvider.generateToken(user);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetailsService.loadUserByUsername(user.getEmail()));

            return AuthResponseDTO.builder()
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .type("Bearer")
                    .id(user.getId())
                    .email(user.getEmail())
                    .nom(user.getNom())
                    .prenom(user.getPrenom())
                    .fullName(user.getFullName())
                    .roles(user.getRoles())
                    .build();

        } catch (Exception e) {
            throw new BusinessException("Erreur lors du rafraîchissement du token: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponseDTO register(ClientRequestDTO registerRequest) {
        // Create the client using ClientService
        clientService.createClient(registerRequest);

        User user = userRepository.findByEmail(registerRequest.getEmail())
                .orElseThrow(() -> new BusinessException("Erreur lors de la création du compte"));

        // Generate JWT tokens
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetailsService.loadUserByUsername(user.getEmail()));

        // Update last login
        user.updateLastLogin();
        userRepository.save(user);

        return AuthResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .build();
    }
}

