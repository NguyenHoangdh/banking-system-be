package com.nghoang.banking.controller;

import com.nghoang.banking.dto.ApiResponse;
import com.nghoang.banking.dto.request.IntrospectRequest;
import com.nghoang.banking.dto.response.AuthenticationResponse;
import com.nghoang.banking.dto.request.LoginRequest;
import com.nghoang.banking.dto.request.LogoutRequest;
import com.nghoang.banking.dto.response.IntrospectResponse;
import com.nghoang.banking.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/auth")
@Tag(name = "Authentication APIs", description = "Các API quản lý xác thực người dùng")
@SecurityRequirements
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest loginDto) throws JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .result(authenticationService.login(loginDto))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder()
                .code(1000)
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Logout successfully!")
                .result(authenticationService.logout(request))
                .build();
    }

}
