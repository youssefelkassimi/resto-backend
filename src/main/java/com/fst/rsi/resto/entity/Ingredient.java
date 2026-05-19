package com.fst.rsi.resto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ingredient")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIngredient;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteStock;

    @Column(nullable = false, length = 20)
    private String unite;

    @Column(precision = 10, scale = 2)
    private BigDecimal seuilAlerte;

    @Column
    private LocalDate datePeremption;

    @ManyToOne
    @JoinColumn(name = "idFournisseur")
    @JsonIgnore
    private Fournisseur fournisseur;

    @JsonIgnore
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    private List<CompositionPlat> compositions;

    @JsonIgnore
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    private List<LigneCommandeFournisseur> lignesCommandeFournisseur;
}
