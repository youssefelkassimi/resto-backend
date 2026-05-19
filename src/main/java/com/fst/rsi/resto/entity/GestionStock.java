package com.fst.rsi.resto.entity;

import com.fst.rsi.resto.entity.enums.TypeOperationStock;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gestion_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestionStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGestionStock;

    @ManyToOne
    @JoinColumn(name = "idManager", nullable = false)
    private Manager manager;

    @ManyToOne
    @JoinColumn(name = "idIngredient", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOperationStock typeOperation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;

    @Column(nullable = false)
    private LocalDateTime dateOperation = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantiteAvant;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantiteApres;
}
