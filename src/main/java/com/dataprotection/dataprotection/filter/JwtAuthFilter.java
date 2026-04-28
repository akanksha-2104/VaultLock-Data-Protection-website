package com.dataprotection.dataprotection.filter;

import com.dataprotection.dataprotection.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        // Only process if token exists and SecurityContext isn't already populated
        if (token != null
                && jwtUtil.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String email = jwtUtil.getEmailFromToken(token);
            String role  = jwtUtil.getRoleFromToken(token);

            // Build the authority from the role claim (e.g. "USER" → "ROLE_USER")
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role);

            // Create an authentication token — third arg is the list of permissions
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of(authority));

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            // Set in SecurityContext — after this point, authentication.getName()
            // in controllers returns the email, which is exactly how your existing
            // controllers identify the user (e.g., Authentication authentication param)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract the raw token string from the Authorization header.
     * Expected format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     * Returns null if header is absent or malformed.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7); // strip "Bearer " prefix
        }
        return null;
    }
}