package com.fst.rsi.resto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServeurStatisticsDTO {
    private Long idServeur;
    private String nomComplet;
    private Long totalCommandes;
    private Long commandesServies;
    private Long commandesEnCours;
    private BigDecimal chiffreAffaireGenere;
    private BigDecimal panierMoyen;
    private Long nombreTablesServies;
    private LocalDate dateEmbauche;
    private LocalDateTime dateDerniereCommande;
    private Boolean actif;
}