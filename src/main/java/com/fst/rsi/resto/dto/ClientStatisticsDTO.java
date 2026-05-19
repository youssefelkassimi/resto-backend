package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.StatutClient;
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
public class ClientStatisticsDTO {
    private Long totalCommandes;
    private Long commandesTerminees;
    private Long commandesAnnulees;
    private BigDecimal depenseTotal;
    private BigDecimal panierMoyen;
    private Integer pointsFidelite;
    private StatutClient statutFidelite;
    private LocalDate dateInscription;
    private LocalDateTime dateDerniereCommande;
    private String platPrefere;
}