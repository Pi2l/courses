package org.m.courses.api.v1.controller.authorization;

public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private Integer accessTokenLifetimeMinutes;
    private Integer refreshTokenLifetimeMinutes;

    public AuthenticationResponse(String generatedAccessToken, String generatedRefreshToken, Integer accessTokenExpirationInMinutes, Integer refreshTokenExpirationInMinutes) {
        accessToken = generatedAccessToken;
        refreshToken = generatedRefreshToken;
        accessTokenLifetimeMinutes = accessTokenExpirationInMinutes;
        refreshTokenLifetimeMinutes = refreshTokenExpirationInMinutes;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getAccessTokenLifetimeMinutes() {
        return accessTokenLifetimeMinutes;
    }

    public void setAccessTokenLifetimeMinutes(int accessTokenLifetimeMinutes) {
        this.accessTokenLifetimeMinutes = accessTokenLifetimeMinutes;
    }

    public int getRefreshTokenLifetimeMinutes() {
        return refreshTokenLifetimeMinutes;
    }

    public void setRefreshTokenLifetimeMinutes(int refreshTokenLifetimeMinutes) {
        this.refreshTokenLifetimeMinutes = refreshTokenLifetimeMinutes;
    }
}
