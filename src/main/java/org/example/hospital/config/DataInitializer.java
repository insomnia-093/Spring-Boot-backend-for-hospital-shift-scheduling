package org.example.hospital.config;

import java.util.EnumSet;
import java.util.Set;
import org.example.hospital.domain.Role;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.repository.RoleRepository;
import org.example.hospital.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.enabled:true}")
    private boolean adminSeedEnabled;

    @Value("${app.init.admin.email:admin@hospital.local}")
    private String adminEmail;

    @Value("${app.init.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.init.admin.full-name:System Admin}")
    private String adminFullName;

    public DataInitializer(RoleRepository roleRepository,
                           UserAccountRepository userAccountRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            ensureRoles();
            if (adminSeedEnabled) {
                ensureAdminUser();
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureRoles() {
        for (RoleType roleType : RoleType.values()) {
            roleRepository.findByName(roleType).orElseGet(() -> roleRepository.save(new Role(roleType)));
        }
    }

    private void ensureAdminUser() {
        if (userAccountRepository.existsByEmail(adminEmail)) {
            return;
        }
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException("Admin seed requires app.init.admin.email and app.init.admin.password");
        }
        UserAccount admin = new UserAccount(adminEmail, passwordEncoder.encode(adminPassword), adminFullName);
        Set<Role> adminRoles = EnumSet.of(RoleType.ADMIN).stream()
                .map(roleType -> roleRepository.findByName(roleType).orElseGet(() -> roleRepository.save(new Role(roleType))))
                .collect(java.util.stream.Collectors.toSet());
        admin.setRoles(adminRoles);
        userAccountRepository.save(admin);
    }
}
