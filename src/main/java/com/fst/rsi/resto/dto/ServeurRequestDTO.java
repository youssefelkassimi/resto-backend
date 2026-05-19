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
public class ServeurRequestDTO {

//    @NotNull(message = "L'ID du serveur est obligatoire")
    private Long idServeur;


    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Veuillez fournir une adresse email valide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(min = 10, max = 20, message = "Le téléphone doit contenir entre 10 et 20 caractères")
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    private String adresse;
    private String photo;

    private LocalDate dateEmbauche;

    @DecimalMin(value = "0.0", message = "Le salaire ne peut pas être négatif")
    private BigDecimal salaire;


    @Min(value = 0, message = "Le nombre d'heures ne peut pas être négatif")
    @Max(value = 60, message = "Le nombre d'heures ne peut pas dépasser 60")
    private Integer nombreHeuresSemaine;
}