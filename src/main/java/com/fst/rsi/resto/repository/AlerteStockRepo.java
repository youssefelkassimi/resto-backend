package com.fst.rsi.resto.repository;


import com.fst.rsi.resto.entity.AlerteStock;
import com.fst.rsi.resto.entity.Ingredient;
import com.fst.rsi.resto.entity.enums.StatutAlerte;
import com.fst.rsi.resto.entity.enums.TypeAlerte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlerteStockRepo extends JpaRepository<AlerteStock, Long> {
    List<AlerteStock> findByIngredient(Ingredient ingredient);
    List<AlerteStock> findByStatut(StatutAlerte statut);
    List<AlerteStock> findByTypeAlerte(TypeAlerte typeAlerte);

    List<AlerteStock> findByStatutIn(List<StatutAlerte> nonTraitee);

    List<AlerteStock> findByIngredientIdIngredientAndStatutIn(Long idIngredient, List<StatutAlerte> nonTraitee);

    List<AlerteStock> findByIngredientIdIngredient(Long idIngredient);

    long countByStatutIn(List<StatutAlerte> nonTraitee);
}

