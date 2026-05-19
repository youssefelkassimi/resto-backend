package com.fst.rsi.resto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceLivreurDTO {
    private Long idPerformance;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreLivraisonsEffectuees;
    private Integer nombreLivraisonsAnnulees;
    private BigDecimal tempsLivraisonMoyen;
    private BigDecimal notePonctualite;
    private BigDecimal noteQualite;
    private BigDecimal noteGlobale;
    private String commentaires;
}