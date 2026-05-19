package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Livreur;
import com.fst.rsi.resto.entity.User;
import com.fst.rsi.resto.entity.enums.StatutLivreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LivreurRepo extends JpaRepository<Livreur, Long> {
    Optional<Livreur> findByUser(User user);
    List<Livreur> findByDisponibleTrue();
    List<Livreur> findByStatut(StatutLivreur statut);


    Optional<Livreur> findByUserEmail(String email);

    Optional<Livreur> findByUserTelephone(String telephone);

    List<Livreur> findByDisponibleTrueAndStatut(StatutLivreur statut);


    List<Livreur> findByDateEmbaucheAfter(LocalDate date);

    @Query("SELECT l FROM Livreur l WHERE " +
            "LOWER(l.user.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.user.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Livreur> searchByNomOrPrenom(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(l) FROM Livreur l WHERE l.disponible = true AND l.statut = 'ACTIF'")
    Long countDisponibles();
}