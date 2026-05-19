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
public class CompositionPlatDTO {
    private Long idPlat;
    private Long idIngredient;
    private String nomIngredient;
    private BigDecimal quantiteNecessaire;
    private String unite;
}


