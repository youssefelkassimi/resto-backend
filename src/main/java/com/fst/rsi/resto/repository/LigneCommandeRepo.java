package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Commande;
import com.fst.rsi.resto.entity.LigneCommande;
import com.fst.rsi.resto.entity.Plat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneCommandeRepo extends JpaRepository<LigneCommande, Long> {
    List<LigneCommande> findByCommande(Commande commande);
    List<LigneCommande> findByPlat(Plat plat);
}