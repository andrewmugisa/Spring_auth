package org.wigo.auth.response;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String token;
    private final long expiresIn;

    public LoginResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }
}
