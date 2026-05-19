package com.fst.rsi.resto.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "serveur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Serveur {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServeur;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDate dateEmbauche = LocalDate.now();

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaire;


    @Column
    private Integer nombreHeuresSemaine;

    @OneToMany(mappedBy = "serveur", cascade = CascadeType.ALL)
    private List<Commande> commandes;

    @OneToMany(mappedBy = "serveur", cascade = CascadeType.ALL)
    private List<PerformanceServeur> performances;
}
