package com.fst.rsi.resto.dto;

import jakarta.validation.constraints.*;
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
public class LivreurRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(min = 10, max = 20)
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8)
    private String password;

    private String adresse;
    private String photo;

    @NotBlank(message = "Le véhicule est obligatoire")
    private String vehicule;



    private LocalDate dateEmbauche;

    @DecimalMin(value = "0.0")
    private BigDecimal salaireBase;

    @DecimalMin(value = "0.0")
    private BigDecimal primeParLivraison;
}