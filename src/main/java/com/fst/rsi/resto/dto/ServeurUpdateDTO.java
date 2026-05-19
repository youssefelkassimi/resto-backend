package com.fst.rsi.resto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServeurUpdateDTO {

@Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
private String nom;

@Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
private String prenom;

@Size(min = 10, max = 20, message = "Le téléphone doit contenirentre 10 et 20 caractères")
private String telephone;
private String adresse;
private String photo;

@DecimalMin(value = "0.0", message = "Le salaire ne peut pas être négatif")
private BigDecimal salaire;

private String zoneAssignee;

@Min(value = 0, message = "Le nombre d'heures ne peut pas être négatif")
@Max(value = 60, message = "Le nombre d'heures ne peut pas dépasser 60")
private Integer nombreHeuresSemaine;
}