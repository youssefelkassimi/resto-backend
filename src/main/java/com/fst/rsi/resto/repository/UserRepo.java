package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.User;
import com.fst.rsi.resto.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTelephone(String telephone);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);

    List<User> findByRolesContaining(UserRole role);

    List<User> findByEnabledTrue();
}