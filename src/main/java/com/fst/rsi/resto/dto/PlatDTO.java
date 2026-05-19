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
public class PlatDTO {
    private Long idPlat;
    private String nom;
    private String description;
    private BigDecimal prix;
    private String categorie;
    private Integer tempsPreparation;
    private Boolean disponible;
    private String image;
}