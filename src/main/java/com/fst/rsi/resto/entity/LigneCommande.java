package com.fst.rsi.resto.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ligne_commande")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigne;

    @ManyToOne
    @JoinColumn(name = "idCommande", nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "idPlat", nullable = false)
    private Plat plat;

    @Column(nullable = false)
    private Integer quantite = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sousTotal;

    @PrePersist
    @PreUpdate
    public void calculerSousTotal() {
        if (prixUnitaire != null && quantite != null) {
            this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}
