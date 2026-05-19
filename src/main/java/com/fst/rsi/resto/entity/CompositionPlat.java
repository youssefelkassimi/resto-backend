package com.fst.rsi.resto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "composition_plat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CompositionPlatId.class)
public class CompositionPlat {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_plat", nullable = false)
    @JsonIgnore
    private Plat plat;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_ingredient", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteNecessaire;
}
