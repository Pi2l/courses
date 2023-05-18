package org.m.courses.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "refresh_token")
public class RefreshToken implements Identity<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String token;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String login;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken token = (RefreshToken) o;
        return id.equals(token.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
