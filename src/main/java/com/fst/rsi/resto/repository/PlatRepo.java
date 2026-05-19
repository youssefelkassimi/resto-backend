package com.fst.rsi.resto.repository;


import com.fst.rsi.resto.entity.Plat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PlatRepo extends JpaRepository<Plat, Long> {
    List<Plat> findByDisponibleTrue();
    List<Plat> findByCategorie(String categorie);
    List<Plat> findByPrixBetween(BigDecimal minPrice, BigDecimal maxPrice);

    boolean existsByNom(String nom);



    List<Plat> findByCategorieAndDisponibleTrue(String categorie);

    List<Plat> findByNomContainingIgnoreCase(String nom);

    @Query("SELECT p FROM Plat p WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Plat> searchByNomOrDescription(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Plat p LEFT JOIN p.lignesCommande lc " +
            "GROUP BY p.idPlat ORDER BY COUNT(lc) DESC")
    List<Plat> findMostPopularPlats(Pageable pageable);

    @Query("SELECT DISTINCT p.categorie FROM Plat p ORDER BY p.categorie")
    List<String> findDistinctCategories();

    List<Plat> findByDateCreationAfter(LocalDate dateLimit, Pageable pageable);
}
