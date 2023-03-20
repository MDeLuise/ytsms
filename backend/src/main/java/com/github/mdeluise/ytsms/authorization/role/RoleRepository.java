package com.github.mdeluise.ytsms.authorization.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(com.github.mdeluise.ytsms.authorization.role.ERole name);
}
