package org.wigo.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.wigo.auth.dto.LoginUserDto;
import org.wigo.auth.dto.RegisterUserDto;
import org.wigo.auth.dto.VerifyUserDto;
import org.wigo.auth.model.AuthUser;
import org.wigo.auth.model.AuthUserFactory;
import org.wigo.auth.repository.AuthUserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final int VERIFICATION_EXPIRY_MINUTES = 15;

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuthUserFactory userFactory;

    public AuthenticationService(
            AuthUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService,
            AuthUserFactory userFactory
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.userFactory = userFactory;
    }

    public AuthUser register(RegisterUserDto input) {
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String username = generateUniqueUsername(input.getName());

        AuthUser user = userFactory.create(
                username,
                input.getEmail(),
                passwordEncoder.encode(input.getPassword()),
                input.getName()
        );
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiration(Instant.now().plus(VERIFICATION_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        user.setEnabled(false);

        AuthUser saved = (AuthUser) userRepository.save(user);
        sendVerificationEmail(saved);
        return saved;
    }

    private String generateUniqueUsername(String name) {
        String base = name.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9]", "");

        if (base.isBlank()) base = "user";

        String candidate = base;
        int attempts = 0;

        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + "_" + (1000 + new Random().nextInt(9000));
            if (++attempts > 10) {
                candidate = base + "_" + UUID.randomUUID().toString().substring(0, 6);
                break;
            }
        }

        return candidate;
    }

    public AuthUser authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );

        AuthUser user = (AuthUser) userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new IllegalStateException("Account not verified. Please check your email");
        }

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        AuthUser user = (AuthUser) userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new IllegalStateException("Account is already verified");
        }

        if (user.getVerificationCodeExpiration() == null
                || Instant.now().isAfter(user.getVerificationCodeExpiration())) {
            throw new IllegalStateException("Verification code has expired. Please request a new one");
        }

        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
            throw new IllegalArgumentException("Verification code does not match");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);
    }

    public void resendVerificationCode(String email) {
        AuthUser user = (AuthUser) userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new IllegalStateException("Account is already verified");
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiration(Instant.now().plus(VERIFICATION_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        userRepository.save(user);
        sendVerificationEmail(user);
    }

    private void sendVerificationEmail(AuthUser user) {
        String subject = "Your Verification Code";
        String code = user.getVerificationCode();
        String html = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color:#f5f5f5; margin:0; padding:0;">
                  <div style="max-width:600px; margin:0 auto; padding:20px;">
                    <h2 style="color:#333; text-align:center;">Verify your account</h2>
                    <p style="font-size:16px; color:#555; text-align:center;">Use the code below to verify your account:</p>
                    <div style="background:#fff; padding:20px; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1); text-align:center;">
                      <p style="font-size:28px; font-weight:bold; color:#007bff; letter-spacing:6px;">%s</p>
                    </div>
                    <p style="font-size:13px; color:#888; text-align:center; margin-top:20px;">
                      This code expires in %d minutes. If you didn't request this, ignore this email.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(code, VERIFICATION_EXPIRY_MINUTES);

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, html);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}
