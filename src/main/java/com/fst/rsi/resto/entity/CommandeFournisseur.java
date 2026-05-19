package com.fst.rsi.resto.entity;

import com.fst.rsi.resto.entity.enums.StatutCommandeFournisseur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commande_fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandeFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCommandeFournisseur;

    @ManyToOne
    @JoinColumn(name = "idFournisseur", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne
    @JoinColumn(name = "idManager")
    private Manager validePar;

    @Column(nullable = false)
    private LocalDate dateCommande = LocalDate.now();

    @Column
    private LocalDate dateLivraisonPrevue;

    @Column
    private LocalDateTime dateValidation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommandeFournisseur statut = StatutCommandeFournisseur.EN_ATTENTE;

    @Column(precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(columnDefinition = "TEXT")
    private String commentaireManager;

    @OneToMany(mappedBy = "commandeFournisseur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommandeFournisseur> lignesCommande;
}
