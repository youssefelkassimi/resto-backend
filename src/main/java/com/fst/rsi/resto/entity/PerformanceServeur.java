package com.fst.rsi.resto.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "performance_serveur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceServeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPerformance;

    @ManyToOne
    @JoinColumn(name = "idServeur", nullable = false)
    private Serveur serveur;

    @ManyToOne
    @JoinColumn(name = "idManager", nullable = false)
    private Manager evaluePar;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column
    private Integer nombreCommandesServies;

    @Column(precision = 10, scale = 2)
    private BigDecimal chiffreAffaireGenere;

    @Column(precision = 10, scale = 2)
    private BigDecimal panierMoyen;

    @Column(precision = 3, scale = 2)
    private BigDecimal noteQualiteService;

    @Column(precision = 3, scale = 2)
    private BigDecimal noteRapidite;

    @Column(precision = 3, scale = 2)
    private BigDecimal noteGlobale;

    @Column(columnDefinition = "TEXT")
    private String commentaires;

    @Column(columnDefinition = "TEXT")
    private String pointsForts;

    @Column(columnDefinition = "TEXT")
    private String axesAmelioration;
}
