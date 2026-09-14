package com.nghoang.banking.unit.service;

import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.dto.request.IntrospectRequest;
import com.nghoang.banking.dto.request.LoginRequest;
import com.nghoang.banking.dto.request.LogoutRequest;
import com.nghoang.banking.dto.response.AuthenticationResponse;
import com.nghoang.banking.dto.response.IntrospectResponse;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.repository.InvalidatedTokenRepository;
import com.nghoang.banking.repository.UserRepository;
import com.nghoang.banking.service.impl.AuthenticationServiceImpl;
import com.nghoang.banking.service.impl.EmailService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationServiceImplTest {
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;
    @Mock
    InvalidatedTokenRepository invalidatedTokenRepository;
    @Mock
    EmailService emailService;

    @InjectMocks
    AuthenticationServiceImpl authenticationService;
    LoginRequest loginRequest;
    User user;
    IntrospectRequest introspectRequest;
    LogoutRequest logoutRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY",
                "test-secret-key-must-be-at-least-512-bits-long-for-HS512-algorithm");
        ReflectionTestUtils.setField(authenticationService, "EXPIRATION_DURATION", 1L);
        loginRequest = LoginRequest.builder()
                .email("")
                .password("")
                .build();
        logoutRequest = LogoutRequest.builder()
                .token("")
                .build();
        user = User.builder()
                .email("lea@gmail.com")
                .firstName("A")
                .lastName("Lê")
                .accountBalance(BigDecimal.ZERO)
                .password("")
                .status("ACTIVE")
                .accountNumber("")
                .build();
        introspectRequest = IntrospectRequest.builder()
                .build();
    }

    @Test
    void test_login_userNotExisted() {
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class, () -> authenticationService.login(loginRequest));

        assertEquals(ErrorCode.USER_NOT_EXISTED, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void test_login_wrongPassword() {
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        AppException appException = assertThrows(AppException.class, () -> authenticationService.login(loginRequest));

        assertEquals(ErrorCode.UNAUTHENTICATED, appException.getErrorCode());

        verify(userRepository, times(1)).findUserByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
        verify(emailService, never()).sendEmail(any());

    }

    @Test
    void test_login_success() throws JOSEException {
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        AuthenticationResponse response = authenticationService.login(loginRequest);

        assertNotNull(response.getToken());
        assertTrue(response.isAuthenticated());

        verify(userRepository, times(1)).findUserByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
        verify(emailService, times(1)).sendEmail(any(EmailDetails.class));
    }

    @Test
    void test_login_emailSendFailureShouldNotBlockLogin() throws JOSEException {
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        doThrow(new RuntimeException()).when(emailService).sendEmail(any(EmailDetails.class));

        AuthenticationResponse response = authenticationService.login(loginRequest);

        assertNotNull(response.getToken());
        assertTrue(response.isAuthenticated());

        verify(userRepository, times(1)).findUserByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
        verify(emailService, times(1)).sendEmail(any(EmailDetails.class));
    }

    @Test
    void test_introspect_tokenBlank() {
        introspectRequest.setToken("");
        IntrospectResponse response = authenticationService.introspect(introspectRequest);

        assertFalse(response.isValid());
    }

    @Test
    void test_introspect_tokenNull() {
        IntrospectResponse response = authenticationService.introspect(introspectRequest);

        assertFalse(response.isValid());
    }

    @Test
    void test_introspect_tokenExpired() throws JOSEException {
        ReflectionTestUtils.setField(authenticationService, "EXPIRATION_DURATION", -1L);
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        String expiredToken = authenticationService.login(loginRequest).getToken();

        introspectRequest.setToken(expiredToken);
        IntrospectResponse introspectResponse = authenticationService.introspect(introspectRequest);

        assertFalse(introspectResponse.isValid());
        verifyNoInteractions(invalidatedTokenRepository);

    }

    @Test
    void test_introspect_tokenInvalidated() throws JOSEException, ParseException {
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        String validToken = authenticationService.login(loginRequest).getToken();

        String jwtId = SignedJWT.parse(validToken).getJWTClaimsSet().getJWTID();
        when(invalidatedTokenRepository.existsById(jwtId)).thenReturn(true);

        introspectRequest.setToken(validToken);
        IntrospectResponse response = authenticationService.introspect(introspectRequest);

        assertFalse(response.isValid());
        verify(invalidatedTokenRepository, times(1)).existsById(jwtId);
    }

    @Test
    void test_introspect_success() throws JOSEException, ParseException {
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        String validToken = authenticationService.login(loginRequest).getToken();

        String jwtId = SignedJWT.parse(validToken).getJWTClaimsSet().getJWTID();
        when(invalidatedTokenRepository.existsById(jwtId)).thenReturn(false);
        introspectRequest.setToken(validToken);
        IntrospectResponse introspectResponse = authenticationService.introspect(introspectRequest);

        assertTrue(introspectResponse.isValid());

        verify(invalidatedTokenRepository, times(1)).existsById(jwtId);
    }

    @Test
    void test_introspect_malformedToken() {
        introspectRequest.setToken("not.a.jwt.at.all");
        IntrospectResponse introspectResponse = authenticationService.introspect(introspectRequest);

        assertFalse(introspectResponse.isValid());
        verifyNoInteractions(invalidatedTokenRepository);
    }

    @Test
    void test_logout_expiredToken() throws JOSEException, ParseException {
        ReflectionTestUtils.setField(authenticationService, "EXPIRATION_DURATION", -1L);
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        String expiredToken = authenticationService.login(loginRequest).getToken();

        logoutRequest.setToken(expiredToken);

        Void logoutResponse = authenticationService.logout(logoutRequest);

        assertNull(logoutResponse);

        verifyNoInteractions(invalidatedTokenRepository);
    }



    @Test
    void test_logout_success() throws JOSEException, ParseException {
        when(userRepository.findUserByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        String token = authenticationService.login(loginRequest).getToken();
        logoutRequest.setToken(token);

        String jwtId = SignedJWT.parse(token).getJWTClaimsSet().getJWTID();

        Void logoutResponse = authenticationService.logout(logoutRequest);

        assertNull(logoutResponse);
        verify(invalidatedTokenRepository, times(1)).save(argThat(t -> t.getId().equals(jwtId)));

    }

    @Test
    void test_logout_malformedToken() {
        logoutRequest.setToken("not.a.jwt.at.all");

//        với ParseException, thường không có gì thêm để assert vì đã biết nó là ParseException rồi. Không giống AppException có thêm field errorCode để phân biệt các loại lỗi khác nhau nên ko cần verify nó
        assertThrows(ParseException.class, () -> authenticationService.logout(logoutRequest));
        verifyNoInteractions(invalidatedTokenRepository);
    }



}
