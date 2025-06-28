package com.buildermaster.projecttracker.security.oauth2;

import com.buildermaster.projecttracker.dto.response.ApiResponseDTO;
import com.buildermaster.projecttracker.dto.response.JwtResponseDTO;
import com.buildermaster.projecttracker.model.User;
import com.buildermaster.projecttracker.security.CustomOidcUserPrincipal;
import com.buildermaster.projecttracker.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("=== Authentication Principal Type: {} ===", authentication.getPrincipal().getClass().getName());


            CustomOidcUserPrincipal customPrincipal = (CustomOidcUserPrincipal) authentication.getPrincipal();
            User user = customPrincipal.getUser();

            log.info("OAuth2 login success for: {} with role: {}", user.getUsername(), user.getRole());

            String token = jwtUtil.generateToken(user.getUsername());

            JwtResponseDTO jwtResponse = JwtResponseDTO.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

            ApiResponseDTO<JwtResponseDTO> responseDTO = ApiResponseDTO.success("OAuth2 login successful", jwtResponse);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), responseDTO);
    }
}
