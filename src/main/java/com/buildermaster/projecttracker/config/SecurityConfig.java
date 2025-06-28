package com.buildermaster.projecttracker.config;

import com.buildermaster.projecttracker.filter.JwtAuthFilter;
import com.buildermaster.projecttracker.security.oauth2.CustomOidcUserService;
import com.buildermaster.projecttracker.security.oauth2.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login", "/login.html").permitAll()
                        // Group all Swagger-related paths together
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/api-docs/**",
                                "/api-docs/swagger-config",
                                "/webjars/**").permitAll()  // Allow initial access
                        .requestMatchers("/admin/**","/api/audit/**","/api/projects/**","/api/developers/**").hasRole("ADMIN")
                        .requestMatchers("/api/projects/**","/api/tasks/**").hasRole("MANAGER")
                        .requestMatchers("/api/tasks/**","/api/developers/**").hasRole("DEVELOPER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Don't redirect Swagger-related requests to OAuth
                            if (request.getRequestURI().contains("/swagger") ||
                                    request.getRequestURI().contains("/api-docs") ||
                                    request.getRequestURI().contains("/webjars")) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");
                                response.getWriter().write("{\"message\": \"Please provide JWT token\"}");
                            } else {
                                response.sendRedirect("/oauth2/authorization/google");
                            }
                        })
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Update your CORS configuration to explicitly allow Swagger endpoints
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:[*]"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}

