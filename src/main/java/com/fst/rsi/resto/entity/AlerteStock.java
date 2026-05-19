package com.fst.rsi.resto.entity;

import com.fst.rsi.resto.entity.enums.StatutAlerte;
import com.fst.rsi.resto.entity.enums.TypeAlerte;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "alerte_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlerte;

    @ManyToOne
    @JoinColumn(name = "idIngredient", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte typeAlerte;

    @Column(nullable = false)
    private LocalDateTime dateAlerte = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAlerte statut = StatutAlerte.NON_TRAITEE;

    @ManyToOne
    @JoinColumn(name = "idManagerTraite")
    private Manager traiteePar;

    @Column
    private LocalDateTime dateTraitement;

    @Column(columnDefinition = "TEXT")
    private String actionPrise;

    @Column(columnDefinition = "TEXT")
    private String message;
}
