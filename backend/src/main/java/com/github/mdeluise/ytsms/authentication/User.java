package com.github.mdeluise.ytsms.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mdeluise.ytsms.security.apikey.ApiKey;
import com.github.mdeluise.ytsms.subscription.Subscription;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "user")
@Table(name = "application_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    @NotEmpty
    @Size(min = 3, max = 20)
    private String username;
    @NotEmpty
    @Size(min = 8, max = 120)
    @JsonProperty
    private String password;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<ApiKey> apiKeys = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Subscription> subscriptions = new HashSet<>();


    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }


    public User() {
    }


    @JsonIgnore
    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Set<ApiKey> getApiKeys() {
        return apiKeys;
    }


    public void setApiKeys(Set<ApiKey> apiKeys) {
        this.apiKeys = apiKeys;
    }


    public void addApiKey(ApiKey apiKey) {
        apiKeys.add(apiKey);
    }


    public void removeApiKey(ApiKey apiKey) {
        apiKeys.remove(apiKey);
    }


    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }


    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
