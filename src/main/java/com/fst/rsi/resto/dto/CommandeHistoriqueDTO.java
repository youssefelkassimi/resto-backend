package com.fst.rsi.resto.dto;



import com.fst.rsi.resto.entity.enums.StatutCommande;
import com.fst.rsi.resto.entity.enums.TypeCommande;
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
public class CommandeHistoriqueDTO {
    private Long idCommande;
    private LocalDateTime dateCommande;
    private TypeCommande typeCommande;
    private StatutCommande statut;
    private BigDecimal montantTotal;
    private Integer nombrePlats;
}
