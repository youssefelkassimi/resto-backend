package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.RapportVente;
import com.fst.rsi.resto.entity.enums.TypeRapport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RapportVenteRepo extends JpaRepository<RapportVente, Long> {
    List<RapportVente> findByTypeRapport(TypeRapport typeRapport);
    List<RapportVente> findByDateDebutBetweenOrDateFinBetween(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2);
    List<RapportVente> findByManagerIdManagerOrderByDateGenerationDesc(Long idManager);
}
