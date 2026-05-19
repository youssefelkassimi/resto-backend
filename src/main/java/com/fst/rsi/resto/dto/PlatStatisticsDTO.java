package com.fst.rsi.resto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatStatisticsDTO {
    private Long idPlat;
    private String nomPlat;
    private Long totalCommandes;
    private Integer totalQuantiteVendue;
    private BigDecimal chiffreAffaire;
    private Integer nombreIngredients;
    private Boolean disponible;
}