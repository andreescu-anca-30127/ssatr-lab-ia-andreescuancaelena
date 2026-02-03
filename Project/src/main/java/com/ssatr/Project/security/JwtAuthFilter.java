package com.ssatr.Project.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/")
                || path.startsWith("/public/")
                || path.equals("/health")
                || path.startsWith("/debug/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        System.out.println("REQ " + request.getMethod()
                + " uri=" + request.getRequestURI()
                + " servletPath=" + request.getServletPath());

        String auth = request.getHeader("Authorization");
        System.out.println("AUTH HEADER = " + auth);

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtService.parse(token).getBody();
                System.out.println("JWT claims type=" + claims.get("type", String.class) + " sub=" + claims.getSubject());

                if (!"ACCESS".equals(claims.get("type", String.class))) {
                    response.setStatus(401);
                    return;
                }

                String username = claims.getSubject();
                System.out.println("JWT OK user=" + username);

                var userDetails = userDetailsService.loadUserByUsername(username);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                System.out.println("JWT ERROR:");
                e.printStackTrace();
                response.setStatus(401);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}