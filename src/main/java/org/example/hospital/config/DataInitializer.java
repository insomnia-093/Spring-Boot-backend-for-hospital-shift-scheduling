package  org.example.hospital.config;

import java.util.EnumSet;
import java.util.Set;
import org.example.hospital.domain.Role;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.repository.RoleRepository;
import org.example.hospital.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

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
        ensureRoles();
        ensureAdminUser();
    }

    private void ensureRoles() {
        for (RoleType roleType : RoleType.values()) {
            roleRepository.findByName(roleType).orElseGet(() -> roleRepository.save(new Role(roleType)));
        }
    }

    private void ensureAdminUser() {
        if (userAccountRepository.existsByEmail("admin@hospital.local")) {
            return;
        }
        UserAccount admin = new UserAccount("admin@hospital.local", passwordEncoder.encode("Admin123!"), "System Admin");
        Set<Role> adminRoles = EnumSet.of(RoleType.ADMIN).stream()
                .map(roleType -> roleRepository.findByName(roleType).orElseGet(() -> roleRepository.save(new Role(roleType))))
                .collect(java.util.stream.Collectors.toSet());
        admin.setRoles(adminRoles);
        userAccountRepository.save(admin);
    }
}
