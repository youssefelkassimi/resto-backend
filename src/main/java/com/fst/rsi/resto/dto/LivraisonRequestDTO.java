package com.fst.rsi.resto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class LivraisonRequestDTO {

    @NotNull(message = "L'ID de la commande est obligatoire")
    private Long idCommande;

    private Long idLivreur;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    private String adresseLivraison;

    @DecimalMin(value = "0.0")
    private BigDecimal fraisLivraison;
}