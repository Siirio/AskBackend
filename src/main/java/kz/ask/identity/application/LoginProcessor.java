package kz.ask.identity.application;

import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.ChangeTemporaryPasswordRequest;
import kz.ask.identity.api.dto.LoginRequest;
import kz.ask.identity.domain.IdentityService;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.domain.entity.AuthSession;
import kz.ask.identity.domain.enums.AppRole;
import kz.ask.identity.domain.enums.UserStatus;
import kz.ask.identity.infrastructure.mapper.AuthMapper;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.AuthException;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginProcessor {

    private final IdentityService identityService;
    private final AuthMapper authMapper;

    @Transactional
    public AuthSessionResponse login(LoginRequest req) {
        AppUser user = identityService.findByEmail(req.getEmail());
        if (user == null) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.DELETED) {
            throw new ForbiddenException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        if (!identityService.verifyPassword(req.getPassword(), user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getMustChangePassword()) {
            String authority = roleAuthority(user.getRole());
            Long ttl = identityService.staffActivationSessionTtl();
            AuthSession session = identityService.createSession(user, authority, false, ttl, true);
            return authMapper.toSessionResponse(session, user, null);
        }

        String authority = roleAuthority(user.getRole());
        AuthSession session = identityService.createSession(user, authority, false);
        return authMapper.toSessionResponse(session, user, null);
    }

    @Transactional
    public AuthSessionResponse changeTemporaryPassword(AskPrincipal principal, ChangeTemporaryPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getPasswordConfirmation())) {
            throw new ValidationException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        AppUser user = identityService.findById(principal.getUserId());
        if (user == null || !user.getMustChangePassword()) {
            throw new ValidationException(ErrorCode.PASSWORD_CHANGE_NOT_REQUIRED);
        }

        identityService.activateStaff(user, req.getNewPassword());
        identityService.logout(principal.getUserId());

        String authority = roleAuthority(user.getRole());
        AuthSession session = identityService.createSession(user, authority, false);
        return authMapper.toSessionResponse(session, user, null);
    }

    private String roleAuthority(AppRole role) {
        return "ROLE_" + role.name();
    }
}
