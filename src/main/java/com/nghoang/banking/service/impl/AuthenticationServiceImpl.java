package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.*;
import com.nghoang.banking.dto.request.IntrospectRequest;
import com.nghoang.banking.dto.request.LoginRequest;
import com.nghoang.banking.dto.request.LogoutRequest;
import com.nghoang.banking.dto.response.AuthenticationResponse;
import com.nghoang.banking.dto.response.IntrospectResponse;
import com.nghoang.banking.entity.InvalidatedToken;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.repository.InvalidatedTokenRepository;
import com.nghoang.banking.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService{
    final PasswordEncoder passwordEncoder;
    final UserRepository userRepository;
    @Value("${jwt.signerKey}")
    String SIGNER_KEY;
    final InvalidatedTokenRepository invalidatedTokenRepository;
    final EmailService emailService;
    @Value("${jwt.expiration-hours}")
    long EXPIRATION_DURATION;
    @Override
    public AuthenticationResponse login(LoginRequest loginDto) throws JOSEException {
        User user = userRepository.findUserByEmail(loginDto.getEmail()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(loginDto.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        try { //xử lý nếu lỗi email sẽ throw exception mặc dù credentials đúng
            EmailDetails loginAlert = EmailDetails.builder()
                    .subject("You're logged in")
                    .recipient(loginDto.getEmail())
                    .messageBody("You logged into your account. If you did not initiate this request, please contact your bank")
                    .build();
            emailService.sendEmail(loginAlert);
        } catch (Exception ignored) {
            log.info("Error, cannot send email");
        }
        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .authenticated(true)
                .build();
    }
    private String generateToken(User user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("Banking App")
                .subject(user.getEmail())
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(EXPIRATION_DURATION, ChronoUnit.HOURS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
//        Payload payload = new Payload(claimsSet.toJSONObject()); nếu dùng JWSObject
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new MACSigner(SIGNER_KEY));
        return signedJWT.serialize();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        if (token == null || token.isBlank()) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (JOSEException | ParseException | AppException e) {
            log.warn("Token introspect failed: {}", e.getMessage());
            isValid  = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY);
        SignedJWT signedJWT = SignedJWT.parse(token);
        //SignedJWT là subClass của JWSObject, được thiết kế chuyên biệt cho JWT
        //Tự động parse payload thành JWTClaimsSet → dùng thẳng . đc
        var verify = signedJWT.verify(verifier);
        if (!verify) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime == null || expiryTime.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    @Override
    public Void logout(LogoutRequest logoutRequest) throws JOSEException, ParseException {
        try {
            SignedJWT signedJWT = verifyToken(logoutRequest.getToken());
            invalidatedTokenRepository.save(InvalidatedToken.builder()
                    .id(signedJWT.getJWTClaimsSet().getJWTID())
                    .expiryTime(signedJWT.getJWTClaimsSet().getExpirationTime())
                    .build());
        } catch (AppException e) { //xử lý việc logout token expired bị ném lỗi
            if (!e.getErrorCode().equals(ErrorCode.UNAUTHENTICATED)) {
                throw e;
            }
        }
        return null;
    }

    private String buildScope(User user) {
        if (CollectionUtils.isEmpty(user.getRoles())) return "";
        Set<String> authorities = new LinkedHashSet<>(); //tránh thêm trùng lặp permission của roles
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                authorities.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        authorities.add(permission.getName());
                    });
                }
            });
        }
        return String.join(" ", authorities);
    }
}
