package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Manager;
import com.fst.rsi.resto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManagerRepo extends JpaRepository<Manager, Long> {
    Optional<Manager> findByUser(User user);

    List<Manager> findByActifTrue();

    Optional<Manager> findByUserEmail(String email);

}