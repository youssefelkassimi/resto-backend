package com.fst.rsi.resto.entity;


import com.fst.rsi.resto.entity.enums.ModePaiement;
import com.fst.rsi.resto.entity.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facture")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFacture;

    @OneToOne
    @JoinColumn(name = "idCommande", unique = true, nullable = false)
    private Commande commande;

    @Column(nullable = false)
    private LocalDateTime dateFacture = LocalDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantHT;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTVA;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTTC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement modePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statutPaiement = StatutPaiement.EN_ATTENTE;
}
