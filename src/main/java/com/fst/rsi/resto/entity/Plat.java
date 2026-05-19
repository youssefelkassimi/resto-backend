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
@Table(name = "plat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlat;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false, length = 50)
    private String categorie;

    @Column
    private Integer tempsPreparation;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(length = 255)
    private String image;

    @Column(nullable = false)
    private LocalDate dateCreation = LocalDate.now();

    @OneToMany(mappedBy = "plat", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LigneCommande> lignesCommande;

    @OneToMany(mappedBy = "plat", cascade = CascadeType.ALL)
    private List<CompositionPlat> compositions;
}
