package com.example.user.security.jwt;

import javax.security.sasl.AuthenticationException;

public class JwtFilterException extends AuthenticationException {

    public JwtErrorCode jwtErrorCode;

    public JwtFilterException(JwtErrorCode jwtErrorCode) {
        this.jwtErrorCode = jwtErrorCode;
    }

    public JwtErrorCode getJwtErrorCode() {
        return jwtErrorCode;
    }
}
