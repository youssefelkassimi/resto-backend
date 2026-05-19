package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.CommandeFournisseur;
import com.fst.rsi.resto.entity.Ingredient;
import com.fst.rsi.resto.entity.LigneCommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneCommandeFournisseurRepo extends JpaRepository<LigneCommandeFournisseur, Long> {
    List<LigneCommandeFournisseur> findByCommandeFournisseur(CommandeFournisseur commandeFournisseur);
    List<LigneCommandeFournisseur> findByIngredient(Ingredient ingredient);
}
