package kz.ask.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import kz.ask.identity.api.dto.AuthSessionResponse;
import kz.ask.identity.api.dto.LogoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class AuthCookieResponseAdvice implements ResponseBodyAdvice<Object> {

    private final AuthCookieService authCookieService;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (!(response instanceof ServletServerHttpResponse servletResponse)) {
            return body;
        }
        HttpServletResponse httpResponse = servletResponse.getServletResponse();
        if (body instanceof AuthSessionResponse session
                && session.getAccessToken() != null
                && session.getExpiresAt() != null) {
            authCookieService.write(httpResponse, session.getAccessToken(), session.getExpiresAt());
        } else if (body instanceof LogoutResponse logout && Boolean.TRUE.equals(logout.getSuccess())) {
            authCookieService.clear(httpResponse);
        }
        return body;
    }
}
