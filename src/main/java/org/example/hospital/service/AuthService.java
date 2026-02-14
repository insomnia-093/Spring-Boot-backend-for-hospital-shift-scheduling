package org.example.hospital.service;

import java.util.Set;
import java.util.stream.Collectors;
import org.example.hospital.domain.Department;
import org.example.hospital.domain.Role;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.dto.JwtResponse;
import org.example.hospital.dto.LoginRequest;
import org.example.hospital.dto.RegisterRequest;
import org.example.hospital.repository.DepartmentRepository;
import org.example.hospital.repository.RoleRepository;
import org.example.hospital.repository.UserAccountRepository;
import org.example.hospital.security.JwtTokenProvider;
import org.example.hospital.security.UserAccountDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // ...existing code...
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserAccountRepository userAccountRepository,
                       RoleRepository roleRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        logger.info("开始注册新用户: {}", request.getEmail());
        Assert.notNull(request, "Register request must not be null");
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            logger.warn("邮箱已注册: {}", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }
        UserAccount user = new UserAccount(request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getFullName());
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> {
                        logger.error("科室不存在: {}", request.getDepartmentId());
                        return new IllegalArgumentException("Department not found");
                    });
            user.setDepartment(department);
        } else if (StringUtils.hasText(request.getDepartmentName())) {
            String name = request.getDepartmentName().trim();
            Department department = departmentRepository.findByName(name)
                    .orElseGet(() -> departmentRepository.save(new Department(name, null)));
            user.setDepartment(department);
        }

        Set<RoleType> requestedRoles = request.getRoles() != null ? request.getRoles() : Set.of();
        Set<RoleType> allowedRoles = requestedRoles.stream()
                .filter(roleType -> roleType == RoleType.DOCTOR || roleType == RoleType.NURSE)
                .collect(Collectors.toSet());
        if (allowedRoles.isEmpty()) {
            allowedRoles = Set.of(RoleType.NURSE);
        }

        Set<Role> roles = allowedRoles.stream()
                .map(this::resolveRole)
                .collect(Collectors.toSet());
        user.setRoles(roles);
        UserAccount savedUser = userAccountRepository.save(user);
        logger.info("用户注册成功: {}, ID: {}, 角色: {}", request.getEmail(), savedUser.getId(), allowedRoles);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            String token = jwtTokenProvider.generateToken(authentication);
            logger.info("注册成功并生成令牌: {}", request.getEmail());
            return buildJwtResponse(savedUser, token);
        } catch (Exception e) {
            logger.error("注册后认证失败: {}", request.getEmail(), e);
            throw e;
        }
    }

    public JwtResponse login(LoginRequest request) {
        logger.info("用户尝试登录: {}", request.getEmail());
        Assert.notNull(request, "Login request must not be null");

        // 检查用户是否存在
        boolean userExists = userAccountRepository.existsByEmail(request.getEmail());
        logger.debug("用户存在检查: {} = {}", request.getEmail(), userExists);

        if (!userExists) {
            logger.warn("登录失败 - 用户不存在: {}", request.getEmail());
            throw new IllegalArgumentException("User not found");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            logger.debug("身份验证成功: {}", request.getEmail());

            String token = jwtTokenProvider.generateToken(authentication);
            UserAccountDetails principal = (UserAccountDetails) authentication.getPrincipal();
            UserAccount user = userAccountRepository.findById(principal.getId())
                    .orElseThrow(() -> {
                        logger.error("登录失败 - 认证后用户不存在: {}, ID: {}", request.getEmail(), principal.getId());
                        return new IllegalStateException("User not found after authentication");
                    });

            logger.info("登录成功: {}, 角色: {}", request.getEmail(), user.getRoles());
            return buildJwtResponse(user, token);
        } catch (Exception e) {
            logger.error("登录失败: {} - {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    private Role resolveRole(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(new Role(roleType)));
    }

    private JwtResponse buildJwtResponse(UserAccount user, String token) {
        Set<RoleType> roleTypes = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return new JwtResponse(token, user.getId(), user.getEmail(), user.getFullName(), roleTypes);
    }
}
