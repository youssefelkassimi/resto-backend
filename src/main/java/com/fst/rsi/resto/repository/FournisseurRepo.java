package com.fst.rsi.resto.repository;

import com.fst.rsi.resto.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FournisseurRepo extends JpaRepository<Fournisseur, Long> {
}