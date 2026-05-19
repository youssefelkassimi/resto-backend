package com.fst.rsi.resto.entity;

import com.fst.rsi.resto.entity.enums.StatutLivreur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "livreur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Livreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLivreur;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 50)
    private String vehicule;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(nullable = false)
    private LocalDateTime dateEmbauche = LocalDateTime.now();

    @Column(precision = 10, scale = 2)
    private BigDecimal salaireBase;

    @Column(precision = 10, scale = 2)
    private BigDecimal primeParLivraison;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatutLivreur statut = StatutLivreur.ACTIF;

    @OneToMany(mappedBy = "livreur", cascade = CascadeType.ALL)
    private List<Livraison> livraisons;
}
