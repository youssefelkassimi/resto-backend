package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.Ingredient;
import com.fst.rsi.resto.entity.Manager;
import com.fst.rsi.resto.entity.enums.TypeOperationStock;
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
public class GestionStockDTO {

    private Long idGestionStock;

    private Long managerId;

    private Long ingredientId;

    private TypeOperationStock typeOperation;

    private BigDecimal quantite;

    private LocalDateTime dateOperation = LocalDateTime.now();

    private String motif;

    private BigDecimal quantiteAvant;

    private BigDecimal quantiteApres;
}
