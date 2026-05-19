package com.fst.rsi.resto.entity;


import com.fst.rsi.resto.entity.enums.StatutCommande;
import com.fst.rsi.resto.entity.enums.TypeCommande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commande")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCommande;

    @ManyToOne
    @JoinColumn(name = "idClient")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "idServeur")
    private Serveur serveur;

    @ManyToOne
    @JoinColumn(name = "idTable")
    private TableRestaurant table;

    @Column(nullable = false)
    private LocalDateTime dateCommande = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCommande typeCommande;

    @Column(precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String instructionsSpeciales;

    @Column
    private Integer tempsEstimePreparation;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignesCommande;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Livraison livraison;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Facture facture;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<Notification> notifications;

}
