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
public class PerformanceServeurDTO {
    private Long idPerformance;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreCommandesServies;
    private BigDecimal chiffreAffaireGenere;
    private BigDecimal panierMoyen;
    private BigDecimal noteQualiteService;
    private BigDecimal noteRapidite;
    private BigDecimal noteGlobale;
    private String commentaires;
    private String pointsForts;
    private String axesAmelioration;
}