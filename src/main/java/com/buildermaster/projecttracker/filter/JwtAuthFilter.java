package com.buildermaster.projecttracker.filter;

import com.buildermaster.projecttracker.service.UserService;
import com.buildermaster.projecttracker.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Check if this is a Swagger resource request
        boolean isSwaggerResource = path.contains("/swagger") ||
                path.contains("/api-docs") ||
                path.contains("/webjars");

        String authHeader = request.getHeader("Authorization");

        if (isSwaggerResource) {
            // For Swagger resources, check if it's an API documentation request
            boolean isApiDocsRequest = path.contains("/v3/api-docs") ||
                    path.contains("/swagger-resources") ||
                    path.contains("/api-docs/swagger-config");

            if (isApiDocsRequest) {
                // Allow access to configuration without authentication
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Rest of your existing JWT validation logic...
        String token = null;
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtUtil.extractUsernameFromToken(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(username);

            if (jwtUtil.validateToken(username, userDetails, token)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}
