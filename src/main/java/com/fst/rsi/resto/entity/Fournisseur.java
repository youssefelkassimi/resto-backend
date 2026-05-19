package com.fst.rsi.resto.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFournisseur;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 100)
    private String contact;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL)
    private List<Ingredient> ingredients;

    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL)
    private List<CommandeFournisseur> commandesFournisseur;
}
