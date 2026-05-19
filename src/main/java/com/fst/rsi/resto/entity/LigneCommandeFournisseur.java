package com.fst.rsi.resto.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ligne_commande_fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigneCommandeFournisseur;

    @ManyToOne
    @JoinColumn(name = "idCommandeFournisseur", nullable = false)
    private CommandeFournisseur commandeFournisseur;

    @ManyToOne
    @JoinColumn(name = "idIngredient", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;
}
