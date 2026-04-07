package com.ngtoan.phone_store.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ngtoan.phone_store.entity.User;
import com.ngtoan.phone_store.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin") == null) {

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setFullName("Admin");
                admin.setEmail("admin@gmail.com");
                admin.setRoleId(1);
                admin.setStatus(true);
                admin.setCreatedDate(LocalDateTime.now());

                userRepository.save(admin);

                System.out.println("Admin created!");
            }
        };
    }
}
