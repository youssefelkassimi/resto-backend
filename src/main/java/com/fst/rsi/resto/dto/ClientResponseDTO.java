package com.fst.rsi.resto.dto;


import com.fst.rsi.resto.entity.enums.StatutClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {
    private Long idClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String photo;
    private LocalDate dateInscription;
    private Boolean actif;
    private Integer pointsFidelite;
    private StatutClient statut;
    private String preferences;
    private Boolean emailVerified;
}