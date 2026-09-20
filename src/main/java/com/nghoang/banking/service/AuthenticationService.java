package com.nghoang.banking.service;

import com.nghoang.banking.dto.request.LogoutRequest;
import com.nghoang.banking.dto.response.AuthenticationResponse;
import com.nghoang.banking.dto.request.IntrospectRequest;
import com.nghoang.banking.dto.response.IntrospectResponse;
import com.nghoang.banking.dto.request.LoginRequest;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request);
     AuthenticationResponse login(LoginRequest loginDto) throws JOSEException;
    Void logout(LogoutRequest logoutRequest) throws JOSEException, ParseException;

}
