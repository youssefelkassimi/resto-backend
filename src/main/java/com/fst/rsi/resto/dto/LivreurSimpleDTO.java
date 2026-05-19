package com.fst.rsi.resto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivreurSimpleDTO {
    private Long idLivreur;
    private String nom;
    private String prenom;
    private String telephone;
    private String vehicule;
}