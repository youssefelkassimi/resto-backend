package com.fst.rsi.resto.entity;

import com.fst.rsi.resto.entity.enums.TypeRapport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapport_vente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapportVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRapport;

    @ManyToOne
    @JoinColumn(name = "idManager", nullable = false)
    private Manager manager;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private LocalDateTime dateGeneration = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRapport typeRapport;

    @Column(precision = 10, scale = 2)
    private BigDecimal chiffreAffaireTotal;

    @Column
    private Integer nombreCommandesTotal;

    @Column
    private Integer nombreClientsUniques;

    @Column(precision = 10, scale = 2)
    private BigDecimal panierMoyen;

    @Column(precision = 10, scale = 2)
    private BigDecimal chiffreAffaireSurPlace;

    @Column(precision = 10, scale = 2)
    private BigDecimal chiffreAffaireAEmporter;

    @Column(precision = 10, scale = 2)
    private BigDecimal chiffreAffaireLivraison;

    @Column(columnDefinition = "TEXT")
    private String platsPlusVendus; // JSON format

    @Column(columnDefinition = "TEXT")
    private String observationsManager;

    @Column(columnDefinition = "TEXT")
    private String recommendations;
}

