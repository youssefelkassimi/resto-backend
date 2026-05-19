package com.fst.rsi.resto.entity;


import com.fst.rsi.resto.entity.enums.StatutLivraison;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "livraison")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLivraison;

    @OneToOne
    @JoinColumn(name = "idCommande", unique = true, nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "idLivreur")
    private Livreur livreur;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String adresseLivraison;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLivraison statutLivraison = StatutLivraison.ASSIGNEE;

    @Column
    private LocalDateTime heureDepart;

    @Column
    private LocalDateTime heureArrivee;

    @Column(precision = 10, scale = 2)
    private BigDecimal fraisLivraison = BigDecimal.ZERO;

}
