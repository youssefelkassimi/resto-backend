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
public class LigneCommandeDTO {
    private Long idLigne;
    private String platNom;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private String options;
    private BigDecimal sousTotal;
}
