package com.fst.rsi.resto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
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
public class LivreurUpdateDTO {

    @Size(min = 2, max = 100)
    private String nom;

    @Size(min = 2, max = 100)
    private String prenom;

    @Size(min = 10, max = 20)
    private String telephone;

    private String adresse;
    private String photo;
    private String vehicule;
    private String numeroPermis;

    @Future(message = "La date de validité doit être dans le futur")
    private LocalDate dateValiditePermis;

    @DecimalMin(value = "0.0")
    private BigDecimal salaireBase;

    @DecimalMin(value = "0.0")
    private BigDecimal primeParLivraison;
}