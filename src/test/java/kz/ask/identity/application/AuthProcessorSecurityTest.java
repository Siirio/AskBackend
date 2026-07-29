package kz.ask.identity.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.audit.domain.SignificantEventService;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.identity.api.dto.ChangePasswordRequest;
import kz.ask.identity.api.dto.TwoFactorChangeRequest;
import kz.ask.identity.api.dto.VerifyCodeRequest;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.dto.AppUserDto;
import kz.ask.identity.domain.dto.AuthSessionDto;
import kz.ask.identity.domain.dto.VerificationDto;
import kz.ask.identity.domain.enums.VerificationChannel;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.domain.enums.VerificationPurpose;
import kz.ask.identity.infrastructure.mail.EmailCodeSender;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.identity.infrastructure.security.JwtTokenService;
import kz.ask.shared.error.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthProcessorSecurityTest {

    @Mock
    private IdentityService identityService;
    @Mock
    private BusinessService businessService;
    @Mock
    private EmailCodeSender emailCodeSender;
    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @Mock
    private SessionCapabilitiesProcessor sessionCapabilitiesProcessor;
    @Mock
    private SignificantEventService significantEventService;
    @Mock
    private JwtTokenService jwtTokenService;

    private AuthProcessor processor;
    private UUID userId;
    private UUID sessionId;
    private AskPrincipal principal;

    @BeforeEach
    void setUp() {
        processor = new AuthProcessor(
                identityService,
                businessService,
                emailCodeSender,
                objectMapper,
                sessionCapabilitiesProcessor,
                significantEventService,
                jwtTokenService);
        ReflectionTestUtils.setField(processor, "testMode", false);
        userId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        principal = new AskPrincipal(userId, sessionId, "Customer", "ROLE_USER");
    }

    @Test
    void passwordChangeRequestCreatesChallengeWithoutChangingPassword() {
        AppUserDto user = AppUserDto.builder()
                .id(userId)
                .email("customer@example.com")
                .displayName("Customer")
                .passwordHash("current-hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
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
        when(identityService.findById(userId)).thenReturn(user);
        when(identityService.verifyPassword("current-password", "current-hash")).thenReturn(true);
        when(identityService.encodePassword("new-password")).thenReturn("new-hash");
        when(identityService.createVerification(
                eq(userId),
                eq(user.getEmail()),
                any(),
                any(),
                eq(false),
                any())).thenReturn(challenge);
        processor.requestPasswordChange(
                principal,
                ChangePasswordRequest.builder()
                        .currentPassword("current-password")
                        .newPassword("new-password")
                        .passwordConfirmation("new-password")
                        .build());

        ArgumentCaptor<VerificationPurpose> purpose = ArgumentCaptor.forClass(VerificationPurpose.class);
        verify(identityService).createVerification(
                eq(userId),
                eq(user.getEmail()),
                any(),
                purpose.capture(),
                eq(false),
                any());
        org.junit.jupiter.api.Assertions.assertEquals("PASSWORD_CHANGE", purpose.getValue().name());
        verify(identityService, never()).changePassword(any(), any());
    }

    @Test
    void securityProcessorExposesPurposeSpecificConfirmationOperations() {
        Class<?> twoFactorRequest = org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> Class.forName("kz.ask.identity.api.dto.TwoFactorChangeRequest"));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> AuthProcessor.class.getMethod(
                "confirmPasswordChange",
                AskPrincipal.class,
                kz.ask.identity.api.dto.VerifyCodeRequest.class));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> AuthProcessor.class.getMethod(
                "requestTwoFactorChange",
                AskPrincipal.class,
                twoFactorRequest));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> AuthProcessor.class.getMethod(
                "confirmTwoFactorChange",
                AskPrincipal.class,
                kz.ask.identity.api.dto.VerifyCodeRequest.class));
    }

    @Test
    void passwordConfirmationAppliesHashAndRevokesOnlyOtherSessions() {
        VerificationDto challenge = securityChallenge(VerificationPurpose.PASSWORD_CHANGE, "new-hash");
        stubCurrentSession(challenge);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> processor.confirmPasswordChange(
                principal,
                VerifyCodeRequest.builder().verificationId(challenge.getId()).code("123456").build()));

        List<String> invocations = org.mockito.Mockito.mockingDetails(identityService).getInvocations().stream()
                .map(invocation -> invocation.getMethod().getName())
                .toList();
        org.junit.jupiter.api.Assertions.assertTrue(invocations.contains("changePasswordHash"));
        org.junit.jupiter.api.Assertions.assertTrue(invocations.contains("revokeOtherSessions"));
        org.junit.jupiter.api.Assertions.assertFalse(invocations.contains("logout"));
        org.junit.jupiter.api.Assertions.assertFalse(invocations.contains("createSession"));
    }

    @Test
    void twoFactorEnableRequiresChallengeBeforeStateChange() {
        AppUserDto user = activeUser(false);
        VerificationDto challenge = securityChallenge(VerificationPurpose.TWO_FACTOR_ENABLE, "true");
        when(identityService.findById(userId)).thenReturn(user);
        when(identityService.createVerification(
                eq(userId),
                eq(user.getEmail()),
                any(),
                any(),
                eq(false),
                eq("true"))).thenReturn(challenge);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> processor.requestTwoFactorChange(
                        principal,
                        TwoFactorChangeRequest.builder().enabled(true).build()));

        ArgumentCaptor<VerificationPurpose> purpose = ArgumentCaptor.forClass(VerificationPurpose.class);
        verify(identityService).createVerification(
                eq(userId),
                eq(user.getEmail()),
                any(),
                purpose.capture(),
                eq(false),
                eq("true"));
        org.junit.jupiter.api.Assertions.assertEquals(VerificationPurpose.TWO_FACTOR_ENABLE, purpose.getValue());
        org.junit.jupiter.api.Assertions.assertFalse(
                org.mockito.Mockito.mockingDetails(identityService).getInvocations().stream()
                        .anyMatch(invocation -> invocation.getMethod().getName().equals("toggleTwoFactor")));
    }

    @Test
    void twoFactorConfirmationChecksOwnershipAndPendingState() {
        VerificationDto challenge = securityChallenge(VerificationPurpose.TWO_FACTOR_ENABLE, "true");
        stubCurrentSession(challenge);
        when(identityService.isTwoFactorEnabled(userId)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> processor.confirmTwoFactorChange(
                principal,
                VerifyCodeRequest.builder().verificationId(challenge.getId()).code("123456").build()));

        List<String> invocations = org.mockito.Mockito.mockingDetails(identityService).getInvocations().stream()
                .map(invocation -> invocation.getMethod().getName())
                .toList();
        org.junit.jupiter.api.Assertions.assertTrue(invocations.contains("setTwoFactorEnabled"));
    }

    @Test
    void genericVerificationRejectsAuthenticatedSecurityChallenges() {
        VerificationDto challenge = securityChallenge(VerificationPurpose.PASSWORD_CHANGE, "new-hash");
        when(identityService.verifyCode(
                eq(challenge.getId()),
                eq("123456"),
                org.mockito.ArgumentMatchers.nullable(UUID.class),
                any(VerificationPurpose[].class))).thenReturn(challenge);

        org.junit.jupiter.api.Assertions.assertThrows(
                ValidationException.class,
                () -> processor.verifyCode(
                        VerifyCodeRequest.builder().verificationId(challenge.getId()).code("123456").build()));
    }

    private VerificationDto securityChallenge(VerificationPurpose purpose, String payload) {
        return VerificationDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .email("customer@example.com")
                .channel(VerificationChannel.EMAIL)
                .purpose(purpose)
                .registrationData(payload)
                .expiresAt(Instant.now().plusSeconds(300))
                .codePlain("123456")
                .build();
    }

    private AppUserDto activeUser(boolean twoFactorEnabled) {
        return AppUserDto.builder()
                .id(userId)
                .email("customer@example.com")
                .displayName("Customer")
                .passwordHash("current-hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .isTwoFactorEnabled(twoFactorEnabled)
                .build();
    }

    private void stubCurrentSession(VerificationDto challenge) {
        AuthSessionDto session = AuthSessionDto.builder()
                .id(sessionId)
                .userId(userId)
                .authority("ROLE_USER")
                .expiresAt(Instant.now().plusSeconds(3600))
                .isRemembered(false)
                .isActivationRequired(false)
                .build();
        when(identityService.verifyCode(
                eq(challenge.getId()),
                eq("123456"),
                eq(userId),
                any(VerificationPurpose[].class))).thenReturn(challenge);
        when(identityService.findById(userId)).thenReturn(activeUser(false));
        when(identityService.findSessionById(sessionId)).thenReturn(session);
        when(jwtTokenService.issue(any(), any())).thenReturn("token");
    }
}
