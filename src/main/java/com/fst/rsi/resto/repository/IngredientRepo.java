package com.fst.rsi.resto.repository;
import com.fst.rsi.resto.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepo extends JpaRepository<Ingredient, Long> {
    long countByQuantiteStockLessThanEqual(BigDecimal zero);

    boolean existsByNom(String nom);

    List<Ingredient> findByNomContainingIgnoreCase(String searchTerm);

    List<Ingredient> findByQuantiteStockLessThanEqual(BigDecimal zero);


    @Query("SELECT i FROM Ingredient i WHERE i.quantiteStock <= i.seuilAlerte")
    List<Ingredient> findByQuantiteStockLessThanEqualSeuilAlerte();

    List<Ingredient> findByDatePeremptionBefore(LocalDate dateLimit);
}

