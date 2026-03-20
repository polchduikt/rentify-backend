package com.rentify.core.security;

import com.rentify.core.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SecurityUser implements UserDetails {

    private final User user;
    private final List<GrantedAuthority> authorities;
    private final boolean accountActive;

    public SecurityUser(User user) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.accountActive = Boolean.TRUE.equals(user.getIsActive());

        Set<? extends GrantedAuthority> mappedAuthorities = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        this.authorities = List.copyOf(mappedAuthorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return accountActive; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return accountActive;
    }

    public User getUser() {
        return user;
    }
}
