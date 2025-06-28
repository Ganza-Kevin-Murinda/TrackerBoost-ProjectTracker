package com.buildermaster.projecttracker.security;

import com.buildermaster.projecttracker.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomOidcUserPrincipal implements OidcUser {
    // Getter for user
    @Getter
    private final User user;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public CustomOidcUserPrincipal(User user, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.user = user;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return idToken.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return getClaims();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

}

