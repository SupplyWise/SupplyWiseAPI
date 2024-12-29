package com.supplywise.supplywise.config;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.net.URL;
import com.supplywise.supplywise.exception.CognitoTokenValidationException;

public class CognitoTokenValidator {

    // Private constructor to hide the implicit public one
    private CognitoTokenValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final String JWK_URL = "https://cognito-idp.eu-west-1.amazonaws.com/eu-west-1_cqV0AHNLS/.well-known/jwks.json";
    private static JWKSource<SecurityContext> keySource;

    static {
        try {
            JWKSet jwkSet = JWKSet.load(new URL(JWK_URL).openStream());
            keySource = new ImmutableJWKSet<>(jwkSet);
        } catch (Exception e) {
            throw new CognitoTokenValidationException("Failed to load JWK set", e);
        }
    }

    public static boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWKMatcher matcher = new JWKMatcher.Builder().keyID(signedJWT.getHeader().getKeyID()).build();
            JWKSelector selector = new JWKSelector(matcher);
            JWK jwk = keySource.get(selector, null).get(0);
            JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey());
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            return false;
        }
    }

    public static JWTClaimsSet getClaims(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet();
        } catch (Exception e) {
            throw new CognitoTokenValidationException("Failed to parse JWT claims", e);
        }
    }
}
