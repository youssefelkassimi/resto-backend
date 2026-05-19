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
public class LivraisonStatisticsDTO {
    private Long totalLivraisons;
    private Long livraisonsEffectuees;
    private Long livraisonsEnCours;
    private Long livraisonsAnnulees;
    private BigDecimal tempsLivraisonMoyen;
    private BigDecimal fraisTotaux;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}