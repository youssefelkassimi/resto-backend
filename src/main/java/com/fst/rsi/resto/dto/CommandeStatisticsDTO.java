package com.fst.rsi.resto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeStatisticsDTO {
    private Long totalCommandes;
    private Long commandesAnnulees;
    private BigDecimal chiffreAffaire;
    private BigDecimal panierMoyen;
    private Long commandesSurPlace;
    private Long commandesAEmporter;
    private Long commandesLivraison;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}