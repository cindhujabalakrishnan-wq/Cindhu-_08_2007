package com.insurance.platform.dto.auth;

/**
 * JWT authentication response returned after register/login.
 */
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private UserInfo user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}
