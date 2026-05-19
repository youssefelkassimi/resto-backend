package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.StatutTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableDTO {
    private Long idTable;
    private Integer numero;
    private Integer capacite;
    private StatutTable statut;
}