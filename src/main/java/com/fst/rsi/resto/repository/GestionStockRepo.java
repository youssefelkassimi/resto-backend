package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.GestionStock;
import com.fst.rsi.resto.entity.Ingredient;
import com.fst.rsi.resto.entity.enums.TypeOperationStock;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GestionStockRepo extends JpaRepository<GestionStock, Long> {
    List<GestionStock> findByIngredient(Ingredient ingredient);
    List<GestionStock> findByTypeOperation(TypeOperationStock typeOperation);
    List<GestionStock> findByDateOperationBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<GestionStock> findByIngredientIdIngredientOrderByDateOperationDesc(Long idIngredient);

    List<GestionStock> findByManagerIdManagerOrderByDateOperationDesc(Long idManager);
}