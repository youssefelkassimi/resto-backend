package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.PerformanceServeur;
import com.fst.rsi.resto.entity.Serveur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceServeurRepo extends JpaRepository<PerformanceServeur, Long> {
    List<PerformanceServeur> findByServeur(Serveur serveur);
    List<PerformanceServeur> findByDateDebutBetweenOrDateFinBetween(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2);


    List<PerformanceServeur> findByServeurIdServeur(Long idServeur);

    List<PerformanceServeur> findByServeurIdServeurAndDateDebutBetween(
            Long idServeur, LocalDate debut, LocalDate fin);

    List<PerformanceServeur> findByEvalueParIdManager(Long idManager);
}
