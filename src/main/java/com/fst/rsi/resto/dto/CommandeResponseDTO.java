package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.StatutCommande;
import com.fst.rsi.resto.entity.enums.TypeCommande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeResponseDTO {
    private Long idCommande;
    private ClientDTO client;
    private ServeurRequestDTO serveur;
    private TableDTO table;
    private LocalDateTime dateCommande;
    private StatutCommande statut;
    private TypeCommande typeCommande;
    private BigDecimal montantTotal;
    private BigDecimal remise;
    private String instructionsSpeciales;
    private Integer tempsEstimePreparation;
    private List<LigneCommandeDTO> lignesCommande;
}