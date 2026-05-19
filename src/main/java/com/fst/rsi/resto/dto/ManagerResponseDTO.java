package com.fst.rsi.resto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerResponseDTO {
    private Long idManager;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String photo;
    private LocalDate dateEmbauche;
    private Boolean actif;
    private BigDecimal salaire;
//    private NiveauAcces niveauAcces;
    private String specialites;
    private Boolean emailVerified;
}
