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

@Service
public class AuthService {

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
        Assert.notNull(request, "Register request must not be null");
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        UserAccount user = new UserAccount(request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getFullName());
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtTokenProvider.generateToken(authentication);
        return buildJwtResponse(savedUser, token);
    }

    public JwtResponse login(LoginRequest request) {
        Assert.notNull(request, "Login request must not be null");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtTokenProvider.generateToken(authentication);
        UserAccountDetails principal = (UserAccountDetails) authentication.getPrincipal();
        UserAccount user = userAccountRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));
        return buildJwtResponse(user, token);
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
