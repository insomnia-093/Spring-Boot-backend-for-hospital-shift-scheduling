package  org.example.hospital.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.example.hospital.domain.Role;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserSummary> findAll() {
        logger.debug("查询所有用户");
        return userAccountRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserSummary findById(Long id) {
        logger.debug("查询用户: {}", id);
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", id);
                    return new IllegalArgumentException("User not found");
                });
        return toSummary(user);
    }

    private UserSummary toSummary(UserAccount user) {
        Set<RoleType> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        String departmentName = user.getDepartment() != null ? user.getDepartment().getName() : null;
        return new UserSummary(user.getId(), user.getEmail(), user.getFullName(), roles, departmentId, departmentName);
    }

    @Transactional
    public void changePassword(Long userId, String rawPassword) {
        logger.info("修改用户密码: {}", userId);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", userId);
                    return new IllegalArgumentException("User not found");
                });
        user.setPassword(passwordEncoder.encode(rawPassword));
        logger.info("密码修改成功: {}", userId);
    }

    public static class UserSummary {
        private final Long id;
        private final String email;
        private final String fullName;
        private final Set<RoleType> roles;
        private final Long departmentId;
        private final String departmentName;

        public UserSummary(Long id, String email, String fullName, Set<RoleType> roles, Long departmentId, String departmentName) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.roles = roles;
            this.departmentId = departmentId;
            this.departmentName = departmentName;
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        public Set<RoleType> getRoles() {
            return roles;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public String getDepartmentName() {
            return departmentName;
        }
    }
}
