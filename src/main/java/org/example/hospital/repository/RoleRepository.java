package org.example.hospital.repository;

import java.util.Optional;
import org.example.hospital.domain.Role;
import org.example.hospital.domain.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);
}
