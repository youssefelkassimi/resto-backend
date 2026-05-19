package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.ModePaiement;
import com.fst.rsi.resto.entity.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureDTO {

    private Long idFacture;

    private Long commandeId;
    private LocalDateTime dateFacture = LocalDateTime.now();
    private BigDecimal montantHT;

    private BigDecimal montantTVA;

    private BigDecimal montantTTC;

    private ModePaiement modePaiement;

    private StatutPaiement statutPaiement = StatutPaiement.EN_ATTENTE;
}