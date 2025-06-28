package com.buildermaster.projecttracker.service;

import com.buildermaster.projecttracker.dto.response.UserResponseDTO;
import com.buildermaster.projecttracker.dto.response.UserSummaryDTO;
import com.buildermaster.projecttracker.model.ERole;
import com.buildermaster.projecttracker.model.User;
import com.buildermaster.projecttracker.security.oauth2.OAuth2UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    Optional<User> getUserByUsername(String email);

    List<UserResponseDTO> getAllUsers();

    User createUser(User user); // for local registration

    User registerOAuth2User(OAuth2UserInfo info); // for OAuth2 callback

    UserSummaryDTO getCurrentUserDetails(); // from SecurityContext

    void assignRoleToUser(UUID userId, ERole role);

}
