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
public class IngredientDTO {
    private Long id;
    private String nom;
    private BigDecimal quantiteStock;
    private String unite;
    private BigDecimal seuilAlerte;
    private LocalDate datePeremption;
    private String fournisseurEmail;
}