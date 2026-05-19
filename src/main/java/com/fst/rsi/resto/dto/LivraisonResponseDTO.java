package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.StatutLivraison;
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
public class LivraisonResponseDTO {
    private Long idLivraison;
    private Long idCommande;
    private LivreurSimpleDTO livreur;
    private String adresseLivraison;
    private StatutLivraison statutLivraison;
    private LocalDateTime heureDepart;
    private LocalDateTime heureArrivee;
    private BigDecimal fraisLivraison;
}