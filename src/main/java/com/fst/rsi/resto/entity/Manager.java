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
@Table(name = "manager")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Manager {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idManager;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDate dateEmbauche = LocalDate.now();

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaire;

//    @Enumerated(EnumType.STRING)
//    @Column(length = 50)
//    private NiveauAcces niveauAcces = NiveauAcces.MANAGER_STANDARD;
//
    @Column(columnDefinition = "TEXT")
    private String specialites;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    private List<GestionStock> gestionsStock;

    @OneToMany(mappedBy = "validePar", cascade = CascadeType.ALL)
    private List<CommandeFournisseur> commandesFournisseursValidees;

}
