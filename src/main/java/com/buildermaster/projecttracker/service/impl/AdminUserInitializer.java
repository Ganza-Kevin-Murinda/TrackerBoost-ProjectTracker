package com.buildermaster.projecttracker.service.impl;

import com.buildermaster.projecttracker.model.EAuthProvider;
import com.buildermaster.projecttracker.model.ERole;
import com.buildermaster.projecttracker.model.User;
import com.buildermaster.projecttracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer {

    @Bean
    public CommandLineRunner createAdminUser(UserRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repo.findByUsername("linuxkevin2024@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setUsername("linuxkevin2024@gmail.com");
                admin.setPassword(passwordEncoder.encode("Admin1")); //Securely store password
                admin.setRole(ERole.ROLE_ADMIN);
                admin.setAuthProvider(EAuthProvider.LOCAL);

                repo.save(admin);
                System.out.println("Default Admin Created");
            }
        };
    }
}
