package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.TableRestaurant;
import com.fst.rsi.resto.entity.enums.StatutTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRestaurantRepo extends JpaRepository<TableRestaurant, Long> {
    List<TableRestaurant> findByStatut(StatutTable statut);

    Optional<TableRestaurant> findByNumero(Integer numero);

    List<TableRestaurant> findByCapacite(Integer capacity);
}