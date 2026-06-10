package org.wigo.auth.response;

import lombok.Getter;

@Getter
public class ApiResponse {
    private final String message;

    public ApiResponse(String message) {
        this.message = message;
    }
}
