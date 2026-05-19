package com.fst.rsi.resto.dto;

import com.fst.rsi.resto.entity.enums.StatutLivreur;
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
public class LivreurResponseDTO {
    private Long idLivreur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String photo;
    private String vehicule;
    private Boolean disponible;
    private LocalDate dateEmbauche;
    private BigDecimal salaireBase;
    private BigDecimal primeParLivraison;
    private StatutLivreur statut;
    private Boolean emailVerified;
}