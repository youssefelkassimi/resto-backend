package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Client;
import com.fst.rsi.resto.entity.enums.StatutClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long> {
    Optional<Client> findByUserEmail(String email);

    Optional<Client> findByUserTelephone(String telephone);

    List<Client> findByActifTrue();

    List<Client> findByStatut(StatutClient statut);

    List<Client> findByStatutIn(List<StatutClient> statuts);

    List<Client> findByUserCreationDateAfter(LocalDate date);


    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.user.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.user.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Client> searchByNomOrPrenom(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.actif = true")
    Long countActiveClients();

    @Query("SELECT c FROM Client c WHERE c.pointsFidelite >= :minPoints")
    List<Client> findByPointsFideliteGreaterThanEqual(@Param("minPoints") Integer minPoints);

    Long countByActifTrue();
}

