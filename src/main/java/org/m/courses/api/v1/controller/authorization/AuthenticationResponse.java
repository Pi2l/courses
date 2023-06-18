package org.m.courses.api.v1.controller.authorization;

public class AuthenticationResponse {
    private String accessToken;
    private Integer accessTokenLifetimeMinutes;
    private Integer refreshTokenLifetimeMinutes;

    public AuthenticationResponse(String generatedAccessToken, Integer accessTokenExpirationInMinutes, Integer refreshTokenExpirationInMinutes) {
        accessToken = generatedAccessToken;
        accessTokenLifetimeMinutes = accessTokenExpirationInMinutes;
        refreshTokenLifetimeMinutes = refreshTokenExpirationInMinutes;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
