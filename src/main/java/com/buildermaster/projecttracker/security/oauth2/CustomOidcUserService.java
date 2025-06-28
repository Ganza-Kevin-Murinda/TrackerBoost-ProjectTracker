package com.buildermaster.projecttracker.security.oauth2;
import com.buildermaster.projecttracker.model.User;
import com.buildermaster.projecttracker.security.CustomOidcUserPrincipal;
import com.buildermaster.projecttracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info(">>> Inside CustomOidcUserService.loadUser");

        // Get user info from OIDC token
        OidcUserInfo userInfo = userRequest.getIdToken().getClaims().containsKey("email")
                ? OidcUserInfo.builder()
                .email((String) userRequest.getIdToken().getClaims().get("email"))
                .name((String) userRequest.getIdToken().getClaims().get("name"))
                .build()
                : null;

        String email = (String) userRequest.getIdToken().getClaims().get("email");
        log.info(">>> Extracted email from OIDC: {}", email);

        // Create OAuth2UserInfo adapter for your existing logic
        OAuth2UserInfo oAuth2UserInfo = new OAuth2UserInfo() {

            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public String getName() {
                return (String) userRequest.getIdToken().getClaims().get("name");
            }
        };

        // Register or fetch existing user using your existing logic
        User user = userService.registerOAuth2User(oAuth2UserInfo);
        log.info(">>> registerOAuth2User returned: {}", user.getId());

        // Create custom OIDC user principal
        return new CustomOidcUserPrincipal(user, userRequest.getIdToken(), userInfo);
    }
}