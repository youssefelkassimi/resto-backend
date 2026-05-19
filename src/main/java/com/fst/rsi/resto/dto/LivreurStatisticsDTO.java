package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.StatutLivreur;
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
public class LivreurStatisticsDTO {
    private Long idLivreur;
    private String nomComplet;
    private Long totalLivraisons;
    private Long livraisonsEffectuees;
    private Long livraisonsEnCours;
    private Long livraisonsAnnulees;
    private BigDecimal tempsLivraisonMoyen;
    private BigDecimal gainsLivraisons;
    private String vehicule;
    private StatutLivreur statut;
    private Boolean disponible;
    private LocalDate dateEmbauche;
    private LocalDateTime dateDerniereLivraison;
}