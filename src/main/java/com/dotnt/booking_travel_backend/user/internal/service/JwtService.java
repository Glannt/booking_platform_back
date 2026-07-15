package com.dotnt.booking_travel_backend.user.internal.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret:default-secret-key-must-be-at-least-256-bits-long-to-be-secure-for-hmac-sha-256}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}") // Default 1 day
    private long jwtExpirationMs;

    public String generateToken(String email, String role) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .claim("role", role)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }

    public String getRoleFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getStringClaim("role");
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && expirationTime.after(new Date());
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }
}
