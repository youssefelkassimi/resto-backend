package com.fst.rsi.resto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompositionPlatRequestDTO {

    @NotNull(message = "L'ID de l'ingrédient est obligatoire")
    private Long idIngredient;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.01", message = "La quantité doit être positive")
    private BigDecimal quantiteNecessaire;
}