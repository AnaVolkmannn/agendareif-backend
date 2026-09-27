package com.reif.agenda_api.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

       if (header == null || !header.startsWith("Bearer ")) {
            System.out.println(">>> Sem header Authorization válido, seguindo como anônimo.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.isValid(token)) {
            System.out.println(">>> Token inválido!");
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.extractClaims(token);
        Long id = claims.get("id", Long.class);
        String role = claims.get("role", String.class);
        boolean mustChangePassword = Boolean.TRUE.equals(claims.get("mustChangePassword", Boolean.class));
        String email = claims.getSubject();

        String path = request.getRequestURI();
        boolean isChangePasswordRoute = path.matches("^/professionals/\\d+/change-password$");

        if (mustChangePassword && !isChangePasswordRoute) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Troca de senha obrigatória antes de continuar.\"}");
            return;
        }

        AuthenticatedUser user = new AuthenticatedUser(id, email, null, role, mustChangePassword);

        var authentication = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println(">>> Autenticado como: " + user.getUsername() + " / role=" + user.getRole());

        filterChain.doFilter(request, response);

        filterChain.doFilter(request, response);
    }
}