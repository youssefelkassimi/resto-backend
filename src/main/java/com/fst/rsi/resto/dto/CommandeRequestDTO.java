package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.TypeCommande;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeRequestDTO {

    @NotNull(message = "L'ID du client est obligatoire")
    private Long idClient;

    private Long idServeur;

    private Long idTable;

    @NotNull(message = "Le type de commande est obligatoire")
    private TypeCommande typeCommande;

    @NotEmpty(message = "La commande doit contenir au moins un plat")
    @Valid
    private List<LigneCommandeRequestDTO> lignesCommande;

    private String instructionsSpeciales;

    private BigDecimal remise;
}