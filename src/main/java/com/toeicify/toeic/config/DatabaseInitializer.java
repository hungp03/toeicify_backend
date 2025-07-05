package com.toeicify.toeic.config;

import com.toeicify.toeic.domain.Role;
import com.toeicify.toeic.domain.User;
import com.toeicify.toeic.repository.RoleRepository;
import com.toeicify.toeic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = Role.builder()
                    .roleId("ADMIN")
                    .roleName("Administrator")
                    .build();

            Role guestRole = Role.builder()
                    .roleId("GUEST")
                    .roleName("Guest User")
                    .build();

            roleRepository.saveAll(List.of(adminRole, guestRole));
            System.out.println("-----INIT ROLES-----");
        } else {
            System.out.println("-----ROLES ALREADY EXIST, SKIP INIT ROLES-----");
        }
    }

    private void initializeAdminUser() {
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findById("ADMIN").orElseThrow(() ->
                    new IllegalStateException("ADMIN role not found. Cannot create admin user.")
            );

            String rawPassword = "123456789";
            String hashedPassword = new BCryptPasswordEncoder().encode(rawPassword);

            User adminUser = User.builder()
                    .userId(1L)
                    .username("admin")
                    .passwordHash(hashedPassword)
                    .email("admin@gmail.com")
                    .fullName("Admin User")
                    .registrationDate(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .role(adminRole)
                    .build();

            userRepository.save(adminUser);
            System.out.println("-----INIT ADMIN USER-----");
        } else {
            System.out.println("-----USERS ALREADY EXIST, SKIP INIT ADMIN USER-----");
        }
    }
}
