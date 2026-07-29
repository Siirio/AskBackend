package kz.ask.identity.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.LoginRequest;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.VerificationDto;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.VerificationPurpose;
import kz.ask.identity.infrastructure.mail.EmailCodeSender;
import kz.ask.identity.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginProcessorTwoFactorTest {

    @Mock
    private IdentityService identityService;
    @Mock
    private BusinessService businessService;
    @Mock
    private EmailCodeSender emailCodeSender;
    @Mock
    private SessionCapabilitiesProcessor sessionCapabilitiesProcessor;
    @Mock
    private JwtTokenService jwtTokenService;

    @Test
    void passwordLoginWithTwoFactorCreatesNoSessionUntilCodeConfirmation() {
        UUID userId = UUID.randomUUID();
        AppUserDto user = AppUserDto.builder()
                .id(userId)
                .email("customer@example.com")
                .displayName("Customer")
                .passwordHash("hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .isPasswordChangeRequired(false)
                .isTwoFactorEnabled(true)
                .build();
        VerificationDto challenge = VerificationDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .email(user.getEmail())
                .channel(VerificationChannel.EMAIL)
                .purpose(VerificationPurpose.LOGIN)
                .expiresAt(Instant.now().plusSeconds(300))
                .codePlain("123456")
                .build();
        when(identityService.findAllByEmail(user.getEmail())).thenReturn(List.of(user));
        when(identityService.verifyPassword("password", "hash")).thenReturn(true);
        when(identityService.createVerification(
                eq(userId),
                eq(user.getEmail()),
                eq(VerificationChannel.EMAIL),
                eq(VerificationPurpose.LOGIN),
                eq(false),
                isNull())).thenReturn(challenge);

        LoginProcessor processor = new LoginProcessor(
                identityService,
                businessService,
                emailCodeSender,
                sessionCapabilitiesProcessor,
                jwtTokenService);
        AuthSessionResponse response = processor.login(
                LoginRequest.builder().email(user.getEmail()).password("password").build());

        org.junit.jupiter.api.Assertions.assertTrue(response.getRequiresTwoFactor());
        org.junit.jupiter.api.Assertions.assertNull(response.getAccessToken());
        verify(identityService, never()).createSession(any(), any(), any());
        verify(identityService, never()).recordLogin(userId);
    }
}
