package com.ngtoan.phone_store.config;

import com.ngtoan.phone_store.security.JwtFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})

            .authorizeHttpRequests(auth -> auth

                // public API
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/users/register").permitAll()
                .requestMatchers("/users/verify-email").permitAll()
                .requestMatchers("/users/resend-verification").permitAll()
                .requestMatchers("/products/**").permitAll()
                .requestMatchers("/chatbot/**").permitAll()
                .requestMatchers("/img/**").permitAll()
                .requestMatchers("/files/**").permitAll()

                // FEEDBACK
                .requestMatchers(HttpMethod.GET, "/api/feedback/product/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/feedback/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/feedback/add").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/api/feedback/update/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/feedback/delete/**").hasAnyRole("USER", "ADMIN")

                // CART
                .requestMatchers("/api/cart/**").hasRole("USER")

                // ORDER
                .requestMatchers("/orders/admin/**").hasRole("ADMIN")
                .requestMatchers("/orders/my-orders").hasRole("USER")
                .requestMatchers("/orders/checkout").hasRole("USER")

                // MEMBERSHIP LEVEL
                .requestMatchers(HttpMethod.GET, "/membership-levels").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/membership-levels/user/me").hasRole("USER")
                .requestMatchers("/membership-levels/admin").hasRole("ADMIN")
                .requestMatchers("/membership-levels/admin/**").hasRole("ADMIN")

                // PAYMENT CALLBACK
                .requestMatchers(HttpMethod.POST, "/payments/*/success").permitAll()
                .requestMatchers(HttpMethod.POST, "/payments/*/fail").permitAll()

                // PAYMENT ADMIN
                .requestMatchers(HttpMethod.GET, "/payments").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/payments/**").hasRole("ADMIN")

                // SUPPLIER
                .requestMatchers("/suppliers/**").hasRole("ADMIN")

                // IMPORT
                .requestMatchers("/imports/**").hasRole("ADMIN")

                // NOTIFICATION
                .requestMatchers(HttpMethod.GET, "/notifications/my").authenticated()
                .requestMatchers(HttpMethod.GET, "/notifications/my/unread-count").authenticated()
                .requestMatchers(HttpMethod.GET, "/notifications/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/notifications/admin/unread-count").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/notifications/*/read").authenticated()

                // USER
                .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/users/me").hasAnyRole("USER", "ADMIN")

                // ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Dùng allowedOriginPatterns để cho phép các domain Vercel preview/production
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://*.vercel.app"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}