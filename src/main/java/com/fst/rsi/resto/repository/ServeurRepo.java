package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Serveur;
import com.fst.rsi.resto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServeurRepo extends JpaRepository<Serveur, Long> {
    Optional<Serveur> findByUser(User user);

    Optional<Serveur> findByUserEmail(String email);

    Optional<Serveur> findByUserTelephone(String telephone);



    List<Serveur> findByActifTrue();


    List<Serveur> findByDateEmbaucheAfter(LocalDate date);

    @Query("SELECT s FROM Serveur s WHERE " +
           "LOWER(s.user.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.user.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Serveur> searchByNomOrPrenom(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(s) FROM Serveur s WHERE s.actif = true")
    Long countActiveServeurs();
}

