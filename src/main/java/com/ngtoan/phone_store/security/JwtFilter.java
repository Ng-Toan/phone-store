package com.ngtoan.phone_store.security;

import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.service.UserService;
import com.ngtoan.phone_store.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("===== JWT FILTER START =====");

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            token = authHeader.substring(7);
            System.out.println("Token: " + token);

            username = JwtUtil.extractUsername(token);
            System.out.println("Extracted username: " + username);
        }

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userService.findByUsername(username);
            System.out.println("User from DB: " + user);

            if (user != null && JwtUtil.validateToken(token, username)) {

                String role = user.getRoleId() == 1 ? "ADMIN" : "USER";
                System.out.println("Detected role: " + role);

                // ✅ tạo authority chuẩn
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                System.out.println("Authorities: " + authorities);

                // ✅ tạo UserDetails (QUAN TRỌNG)
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        authorities
                );

                // ✅ tạo authentication đúng chuẩn
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("Authentication set in SecurityContext");
            }
        }

        System.out.println("===== JWT FILTER END =====");

        filterChain.doFilter(request, response);
    }
}