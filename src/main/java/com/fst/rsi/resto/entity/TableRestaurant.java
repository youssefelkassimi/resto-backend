package com.fst.rsi.resto.entity;


import com.fst.rsi.resto.entity.enums.StatutTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "table_restaurant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableRestaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTable;

    @Column(unique = true, nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTable statut = StatutTable.LIBRE;

}
