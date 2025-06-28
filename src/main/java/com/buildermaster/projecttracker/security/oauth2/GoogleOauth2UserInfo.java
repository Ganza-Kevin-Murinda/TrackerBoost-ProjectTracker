package com.buildermaster.projecttracker.security.oauth2;

import java.util.Map;

public class GoogleOauth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOauth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }

}
