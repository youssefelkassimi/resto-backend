package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.CommandeFournisseur;
import com.fst.rsi.resto.entity.Fournisseur;
import com.fst.rsi.resto.entity.enums.StatutCommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeFournisseurRepo extends JpaRepository<CommandeFournisseur, Long> {
    List<CommandeFournisseur> findByFournisseur(Fournisseur fournisseur);
    List<CommandeFournisseur> findByStatut(StatutCommandeFournisseur statut);
}

