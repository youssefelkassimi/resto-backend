package com.fst.rsi.resto.entity;

import com.fst.rsi.resto.entity.enums.StatutClient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@Table(name="client")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idClient;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private Integer pointsFidelite = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatutClient statut = StatutClient.BRONZE;

    @Column(columnDefinition = "TEXT")
    private String preferences;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Commande> commandes;

    @Column(nullable = false)
    private boolean actif=true;


    public boolean getActif() {
        return actif;
    }
}
