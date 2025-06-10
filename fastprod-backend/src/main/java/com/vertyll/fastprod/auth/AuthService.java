package com.vertyll.fastprod.auth;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.email.EmailService;
import com.vertyll.fastprod.email.EmailTemplateName;
import com.vertyll.fastprod.role.RoleService;
import com.vertyll.fastprod.user.User;
import com.vertyll.fastprod.user.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
class AuthService {

    private final UserService userService;
    private final VerificationTokenRepository tokenRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public void register(RegisterRequestDto request) throws MessagingException {
        if (userService.existsByEmail(request.email())) {
            throw new ApiException("Email already registered", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(roleService.getOrCreateDefaultRole("USER")))
                .enabled(false)
                .build();

        userService.saveUser(user);

        String verificationCode = generateVerificationCode();
        createVerificationToken(user, verificationCode);
        emailService.sendEmail(
                user.getEmail(),
                user.getFirstName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                verificationCode,
                "Account activation"
        );
    }

    public AuthResponseDto authenticate(AuthRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userService.findByEmailWithRoles(request.email())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!user.isEnabled()) {
            throw new ApiException("Account not verified", HttpStatus.FORBIDDEN);
        }

        String jwtToken = jwtService.generateToken(user);
        return AuthResponseDto.mapToDto(jwtToken, "Bearer");
    }

    @Transactional
    public void verifyAccount(String code) {
        VerificationToken verificationToken = tokenRepository.findByToken(code)
                .orElseThrow(() -> new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST));

        if (verificationToken.isUsed()) {
            throw new ApiException("Verification code already used", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Verification code expired", HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        verificationToken.setUsed(true);

        userService.saveUser(user);
        tokenRepository.save(verificationToken);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void createVerificationToken(User user, String token) {
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        tokenRepository.save(verificationToken);
    }
}
