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
                .requestMatchers("/img/**").permitAll()
                .requestMatchers("/files/**").permitAll()

                // FEEDBACK - ai cũng được xem
                .requestMatchers(HttpMethod.GET, "/api/feedback/product/**").permitAll()
                
                // FEEDBACK - admin quản lý tất cả feedback
                .requestMatchers(HttpMethod.GET, "/api/feedback/admin/**").hasRole("ADMIN")

                // FEEDBACK - user đăng nhập mới được đánh giá
                .requestMatchers(HttpMethod.POST, "/api/feedback/add").hasRole("USER")

                // FEEDBACK - user hoặc admin được sửa/xoá
                .requestMatchers(HttpMethod.PUT, "/api/feedback/update/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/feedback/delete/**").hasAnyRole("USER", "ADMIN")
                //Giỏ hàng
                .requestMatchers("/api/cart/**").hasAnyRole("USER")

                 // ORDER - ADMIN quản lý đơn hàng
                .requestMatchers("/orders/admin/**").hasRole("ADMIN")

                // ORDER - USER checkout
                .requestMatchers("/orders/my-orders").hasRole("USER")
                .requestMatchers("/orders/checkout").hasRole("USER")

                // MEMBERSHIP LEVEL - USER / ADMIN xem danh sách level
                .requestMatchers(HttpMethod.GET, "/membership-levels").hasAnyRole("USER", "ADMIN")

                // MEMBERSHIP LEVEL - USER xem hạng của chính mình
                .requestMatchers(HttpMethod.GET, "/membership-levels/user/me").hasRole("USER")

                // MEMBERSHIP LEVEL - ADMIN thêm/sửa/xóa/tính lại level
                .requestMatchers("/membership-levels/admin").hasRole("ADMIN")
                .requestMatchers("/membership-levels/admin/**").hasRole("ADMIN")

                // PAYMENT CALLBACK
                .requestMatchers(HttpMethod.POST, "/payments/*/success").permitAll()
                .requestMatchers(HttpMethod.POST, "/payments/*/fail").permitAll()

                // PAYMENT ADMIN
                .requestMatchers(HttpMethod.GET, "/payments").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/payments/**").hasRole("ADMIN")

                // SUPPLIER - ADMIN quản lý nhà cung cấp
                .requestMatchers("/suppliers/**").hasRole("ADMIN")

                // IMPORT - ADMIN quản lý nhập hàng
                .requestMatchers("/imports/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/notifications/my").authenticated()
                .requestMatchers(HttpMethod.GET, "/notifications/my/unread-count").authenticated()

                .requestMatchers(HttpMethod.GET, "/notifications/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/notifications/admin/unread-count").hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT, "/notifications/*/read").authenticated()

                // USER
                .requestMatchers("/users/**").hasAnyRole("USER","ADMIN")
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
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

        config.setAllowedOrigins(java.util.List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}