package org.wigo.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wigo.auth.dto.LoginUserDto;
import org.wigo.auth.dto.RegisterUserDto;
import org.wigo.auth.dto.ResendVerificationDto;
import org.wigo.auth.dto.VerifyUserDto;
import org.wigo.auth.model.AuthUser;
import org.wigo.auth.response.ApiResponse;
import org.wigo.auth.response.LoginResponse;
import org.wigo.auth.service.AuthenticationService;
import org.wigo.auth.service.JwtService;
import org.wigo.auth.service.TokenBlacklistService;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(
            JwtService jwtService,
            AuthenticationService authenticationService,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        authenticationService.register(registerUserDto);
        return ResponseEntity.ok(new ApiResponse("Registration successful. Please check your email to verify your account."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        AuthUser authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getJwtExpiration());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto) {
        authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok(new ApiResponse("Account verified successfully. You can now log in."));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse> resendVerificationCode(@Valid @RequestBody ResendVerificationDto dto) {
        authenticationService.resendVerificationCode(dto.getEmail());
        return ResponseEntity.ok(new ApiResponse("Verification code resent. Please check your email."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());
            tokenBlacklistService.blacklistToken(token, expiration.toInstant());
        }
        return ResponseEntity.ok(new ApiResponse("Logged out successfully."));
    }
}
